package com.availity.validator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class EnrollmentCSVSpliter {

	public EnrollmentCSVSpliter() {
	}

	static class FileEntry {
		String userId;
		String first_LastName;
		int version;
		String insuranceCompany;

		@Override
		public boolean equals(Object obj) {
			return userId.equals(((FileEntry) obj).userId);
		}

		public static FileEntry parse(String line) {
			FileEntry fe = new FileEntry();

			line.split("", 4);

			return fe;
		}

		@Override
		public String toString() {
			return "FileEntry [userId=" + userId + ", first_LastName=" + first_LastName + ", version=" + version
					+ ", insuranceCompany=" + insuranceCompany + "]";
		}

	}

	public static void main(String[] args) throws Exception {

		String directory = "C:\\tmp\\csv\\";

		System.out.println("searching -> " + directory);

		File[] fs = new File(directory).listFiles((n) -> {
			String name = n.getName();
			return (!name.startsWith("out_") && name.toUpperCase().endsWith(".CSV"));
		});

		if (fs == null)
			return;

		System.out.println("found -> " + fs.length);

		boolean ignorefirstline = true;

		List<FileEntry> records = new ArrayList<>();

		for (File f : fs) {
			if (f.canRead()) {
				try (FileReader fr = new FileReader(f);) {
					Iterable<CSVRecord> csvrecord = CSVFormat.EXCEL.parse(fr);

					for (CSVRecord record : csvrecord) {
						if (ignorefirstline) {
							ignorefirstline = false;
							continue;
						}
						FileEntry fe = new FileEntry();
						fe.userId = record.get(0);
						fe.first_LastName = record.get(1);
						fe.version = Integer.parseInt(record.get(2));
						fe.insuranceCompany = record.get(3);
						records.add(fe);
						System.out.println("v -> " + record);
					}

				}
			}
		}

		// separate enrollees by insurance company in its own file
		Map<String, List<FileEntry>> companyentrmap = new HashMap<>();

		for (FileEntry fe : records) {
			String company = fe.insuranceCompany.toLowerCase().trim();

			List<FileEntry> target;

			if (companyentrmap.containsKey(company)) {
				target = companyentrmap.get(company);
				target.add(fe);
			} else {
				target = new ArrayList<>();
				target.add(fe);
				companyentrmap.put(company, target);
			}
		}

		// System.out.println("companyentrmap size: -> " + companyentrmap.size());
		// System.out.println();

		for (String companies : companyentrmap.keySet()) {

			List<FileEntry> usersInCompany = companyentrmap.get(companies);
			System.out.printf("Companies:[%s], user count [%d]\n", companies, usersInCompany.size());

			// user higher version of the userId
			Map<String, FileEntry> targetmap = new HashMap<>();
			for (FileEntry user : usersInCompany) {
				// System.out.println(">> user: " + user.userId + ", version: " + user.version);
				if (targetmap.containsKey(user.userId)) {
					FileEntry fe = targetmap.get(user.userId);
					if (fe.version < user.version) {
						targetmap.put(user.userId, user);
					}
				} else {
					targetmap.put(user.userId, user);
				}
			}

			Set<String> userIds = targetmap.keySet();

			List<FileEntry> fileentry = userIds.stream().map(s -> targetmap.get(s)).collect(Collectors.toList());

			// sort the contents of each file by last and first name (ascending)
			Collections.sort(fileentry, (a, b) -> a.first_LastName.compareTo(b.first_LastName));

			// Output to seaparate file

			File outfile = new File(directory, "out_" + URLEncoder.encode(companies, "utf-8") + ".csv");
			if (outfile.exists())
				outfile.delete();

			try (FileOutputStream fos = new FileOutputStream(outfile)) {
				for (FileEntry u : fileentry) {
					String pws = String.format("\"%s\",\"%s\",%s,\"%s\"\r\n", u.userId, u.first_LastName, u.version,
							u.insuranceCompany);
					System.out.println(">>>> " + pws);
					fos.write(pws.getBytes());
				}
			}

		}
	}

}
