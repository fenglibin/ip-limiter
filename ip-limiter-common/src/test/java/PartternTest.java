import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartternTest {

	public static void main(String[] args) {
		Pattern r = Pattern.compile("127.*.0.1");
		Matcher matcher = r.matcher("127.0.0.2");
		boolean match = matcher.find();
		System.out.println(match);
	}

}
