import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jsoup.Jsoup;
/**
 * Requires mail.jar and jsoup.jar
 *	
 * @author Goodwin
 *
 */
public class main {

	private static void sendFromGMail(String from, String pass, String to[],
			String subject, String body) {
		Properties props = System.getProperties();
		String host = "smtp.gmail.com";
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", from);
		props.put("mail.smtp.password", pass);
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.auth", "true");

		Session session = Session.getDefaultInstance(props);
		MimeMessage message = new MimeMessage(session);

		try {
			message.setFrom(new InternetAddress(from));
			InternetAddress[] toAddress = new InternetAddress[to.length];

			// To get the array of addresses
			for (int i = 0; i < to.length; i++) {
				toAddress[i] = new InternetAddress(to[i]);
			}

			for (int i = 0; i < toAddress.length; i++) {
				message.addRecipient(Message.RecipientType.TO, toAddress[i]);
			}

			message.setSubject(subject);
			message.setText(body);
			Transport transport = session.getTransport("smtp");
			transport.connect(host, from, pass);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		} catch (AddressException ae) {
			ae.printStackTrace();
		} catch (MessagingException me) {
			me.printStackTrace();
		}
	}

	/**
	 * 
	 * @param s
	 *            - the class to be pulled
	 * @return - an arraylist of pulled strings
	 * @throws IOException
	 */
	public static ArrayList<String> finder(String s) throws IOException {
		URL web = new URL(
				"http://rabi.phys.virginia.edu/mySIS/CS2/page.php?Semester=1148&Type=Group&Group=CompSci&Print=");
		BufferedReader in = new BufferedReader(new InputStreamReader(
				web.openStream()));

		ArrayList<String> out = new ArrayList<String>();
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			if (inputLine.contains(s))
				out.add((inputLine));
		}
		in.close();

		return out;
	}

	/**
	 * Removes html symbols from text
	 * 
	 * @param html
	 *            text
	 * @return non html text
	 */
	public static String html2text(String html) {
		return Jsoup.parse(html).text();
	}

	/**
	 * Initializes three classes (CS6456, CS6610, CS4720)
	 * 
	 * @return - the arraylist of arraylist of strings
	 * @throws IOException
	 */
	public static ArrayList<ArrayList<String>> init() throws IOException {
		ArrayList<ArrayList<String>> courses = new ArrayList<ArrayList<String>>();
		courses.add(finder("CS6456"));
		courses.add(finder("CS6610"));
		courses.add(finder("CS4720"));
		return courses;

	}

	/**
	 * Main - Checks init first, waits five minutes, then checks remaining
	 * classes
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ArrayList<ArrayList<String>> courses = init();
		ArrayList<String> retest = new ArrayList<String>();
		
		while (true) {
			if (!retest.isEmpty()) {
				for (String s : retest) {
					courses.add(finder(s));
				}
				retest.clear();
			}

			for (ArrayList<String> list : courses) {
				for (String s : list) {
					if (s.contains("Lecture")) {
						if (s.contains("Open")) {
							String message = ("Class #"
									+ html2text(s).substring(0, 5)
									+ " is open for " + s.substring(
									s.indexOf("CS"), s.indexOf("CS") + 6));
							System.out.println(message);
							// send mail
							String[] addresses = new String[1];
							addresses[0] = "goodwin616@gmail.com";
							// First string is for username, second string is
							// for password
							sendFromGMail("", "", addresses, message, message);
						} else {
							String class_Code = s.substring(s.indexOf("CS"),
									s.indexOf("CS") + 6);
							System.out.println("Class #"
									+ html2text(s).substring(0, 5)
									+ " is closed for "
									+ s.substring(s.indexOf("CS"),
											s.indexOf("CS") + 6));
							retest.add(class_Code);
						}
					}
				}

			}

			courses.clear();
			if (retest.isEmpty()) {
				break;
			}

			try {
				Thread.sleep(300000);
			} catch (InterruptedException e) {
			}
		}

	}
}
