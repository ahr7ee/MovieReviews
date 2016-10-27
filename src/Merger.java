import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;


public class Merger {
	public static void main(String[] args) throws FileNotFoundException {
		ArrayList<String> list = new ArrayList<String>();
		Scanner fs = new Scanner(new File("results.txt"));
		while(fs.hasNextLine()) {
			String str = fs.nextLine();
			if(!list.contains(str))
				list.add(str);
		}
		fs.close();
		for(String str : list)
			System.out.println(str);
		/*PrintWriter pw = new PrintWriter(new File("unigrams.txt"));
		for(String str : set)
			pw.println(str);
		pw.close();*/
		/*Scanner fs = new Scanner(new File("newlist2.txt"));
		while(fs.hasNextLine())
			System.out.print(fs.next() + ", ");
		fs.close();*/
	}
}
