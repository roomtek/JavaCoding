package com.availity.validator;

import java.util.ArrayList;
import java.util.List;

public class LISPValidator {

	public LISPValidator() {

	}

	public static void main(String[] args) {
		boolean r = LISPValidator.validate("; do loops consist of three parts\r\n"
				+ ";   - a list of local variables, their initial values, and \r\n"
				+ ";     how they get updated at the end of each pass through the loop\r\n"
				+ ";   - the stopping condition for the loop and\r\n"
				+ ";     a list of statements to execute after the loop stops\r\n"
				+ ";     (the return value of do is the return value of the last statement)\r\n"
				+ ";   - the body of the loop: the statements to execute each pass\r\n" + "(do  \r\n"
				+ "     ; initialization/update descriptions\r\n"
				+ "     ( (x 0 (+ x 1))    ; x = 0 initially, add 1 after every pass\r\n"
				+ "       (y 10 (+ y 2)))  ; y = 10 initially, add 2 after every pass\r\n" + "\r\n"
				+ "     ; stopping section\r\n" + "     ( ; stopping condition\r\n"
				+ "       (OR (= x 10) (> y 15))  ; stop if (x == 10) or (y > 15)\r\n"
				+ "       ; actions to perform after stopping\r\n" + "       (format t \"~%exited loop~%\" x)\r\n"
				+ "       (format t \"x stopped at ~A~%\" x)\r\n" + "       (format t \"y stopped at ~A~%\" y)\r\n"
				+ "       (format t \"returning ~A~%\" (+ x y))\r\n" + "       ; return value\r\n"
				+ "       (+ x y))\r\n" + "\r\n" + "    ; loop body\r\n" + "    (format t \"inside loop ~%\")\r\n"
				+ "    (format t \"x is currently ~A~%\" x)\r\n" + "    (format t \"y is currently ~A~%\" y))");
		System.out.println("validate -> " + r);
	}

	final static String COMMENT = ";";

	private static boolean validate(String rawInput) {
		String[] lines = rawInput.split("\r\n");
		List<String> codel = new ArrayList<>();

		for (String line : lines) {
			// ignore comment lines
			if (line.length() == 0 || line.trim().startsWith(COMMENT))
				continue;
			// https://www.gnu.org/software/emacs/manual/html_node/elisp/Comments.html
			int cmt = line.indexOf(COMMENT); // TODO - ensure comment is not enclosed in text ";"

			if (cmt > 0) {
				line = line.substring(0, cmt);// .trim();
			}

			// remove whitespaces
			line = line.replaceAll(" ", "").trim();
			codel.add(line);
		}

		String code = String.join("", codel);
		System.out.printf("code > %s\n", code);

//		for (String l : codel) {
//			System.out.printf("> %s\n", l);
//		}

		char[] arr = code.toCharArray();
		System.out.printf("code length = %s\n", arr.length);

		long opencount = 0;
		long closecount = 0;
		boolean escape = false;

		for (char c : arr) {

			if (c == '"') {
				escape = !escape;
				continue;
			}

			if (escape) {
				// ignore char
			} else {
				if (c == '(') {
					opencount++;
				} else if (c == ')') {
					closecount++;
				} else {
					// code
				}
			}

		}

		System.out.printf("open %d close %d\n", opencount, closecount);

		return opencount == closecount;
	}

}