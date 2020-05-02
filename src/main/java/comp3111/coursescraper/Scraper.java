package comp3111.coursescraper;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.DomText;
import java.util.Vector;
import java.util.stream.Collectors;
import java.time.LocalTime;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * WebScraper provide a sample code that scrape web content. After it is constructed, you can call the method scrape with a keyword, 
 * the client will go to the default url and parse the page by looking at the HTML DOM.  
 * <br>
 * In this particular sample code, it access to HKUST class schedule and quota page (COMP). 
 * <br>
 * https://w5.ab.ust.hk/wcq/cgi-bin/1830/subject/COMP
 *  <br>
 * where 1830 means the third spring term of the academic year 2018-19 and COMP is the course code begins with COMP.
 * <br>
 * Assume you are working on Chrome, paste the url into your browser and press F12 to load the source code of the HTML. You might be freak
 * out if you have never seen a HTML source code before. Keep calm and move on. Press Ctrl-Shift-C (or CMD-Shift-C if you got a mac) and move your
 * mouse cursor around, different part of the HTML code and the corresponding the HTML objects will be highlighted. Explore your HTML page from
 * body &rarr; div id="classes" &rarr; div class="course" &rarr;. You might see something like this:
 * <br>
 * <pre>
 * {@code
 * <div class="course">
 * <div class="courseanchor" style="position: relative; float: left; visibility: hidden; top: -164px;"><a name="COMP1001">&nbsp;</a></div>
 * <div class="courseinfo">
 * <div class="popup attrword"><span class="crseattrword">[3Y10]</span><div class="popupdetail">CC for 3Y 2010 &amp; 2011 cohorts</div></div><div class="popup attrword"><span class="crseattrword">[3Y12]</span><div class="popupdetail">CC for 3Y 2012 cohort</div></div><div class="popup attrword"><span class="crseattrword">[4Y]</span><div class="popupdetail">CC for 4Y 2012 and after</div></div><div class="popup attrword"><span class="crseattrword">[DELI]</span><div class="popupdetail">Mode of Delivery</div></div>	
 *    <div class="courseattr popup">
 * 	    <span style="font-size: 12px; color: #688; font-weight: bold;">COURSE INFO</span>
 * 	    <div class="popupdetail">
 * 	    <table width="400">
 *         <tbody>
 *             <tr><th>ATTRIBUTES</th><td>Common Core (S&amp;T) for 2010 &amp; 2011 3Y programs<br>Common Core (S&amp;T) for 2012 3Y programs<br>Common Core (S&amp;T) for 4Y programs<br>[BLD] Blended learning</td></tr><tr><th>EXCLUSION</th><td>ISOM 2010, any COMP courses of 2000-level or above</td></tr><tr><th>DESCRIPTION</th><td>This course is an introduction to computers and computing tools. It introduces the organization and basic working mechanism of a computer system, including the development of the trend of modern computer system. It covers the fundamentals of computer hardware design and software application development. The course emphasizes the application of the state-of-the-art software tools to solve problems and present solutions via a range of skills related to multimedia and internet computing tools such as internet, e-mail, WWW, webpage design, computer animation, spread sheet charts/figures, presentations with graphics and animations, etc. The course also covers business, accessibility, and relevant security issues in the use of computers and Internet.</td>
 *             </tr>	
 *          </tbody>
 *      </table>
 * 	    </div>
 *    </div>
 * </div>
 *  <h2>COMP 1001 - Exploring Multimedia and Internet Computing (3 units)</h2>
 *  <table class="sections" width="1012">
 *   <tbody>
 *    <tr>
 *        <th width="85">Section</th><th width="190" style="text-align: left">Date &amp; Time</th><th width="160" style="text-align: left">Room</th><th width="190" style="text-align: left">Instructor</th><th width="45">Quota</th><th width="45">Enrol</th><th width="45">Avail</th><th width="45">Wait</th><th width="81">Remarks</th>
 *    </tr>
 *    <tr class="newsect secteven">
 *        <td align="center">L1 (1765)</td>
 *        <td>We 02:00PM - 03:50PM</td><td>Rm 5620, Lift 31-32 (70)</td><td><a href="/wcq/cgi-bin/1830/instructor/LEUNG, Wai Ting">LEUNG, Wai Ting</a></td><td align="center">67</td><td align="center">0</td><td align="center">67</td><td align="center">0</td><td align="center">&nbsp;</td></tr><tr class="newsect sectodd">
 *        <td align="center">LA1 (1766)</td>
 *        <td>Tu 09:00AM - 10:50AM</td><td>Rm 4210, Lift 19 (67)</td><td><a href="/wcq/cgi-bin/1830/instructor/LEUNG, Wai Ting">LEUNG, Wai Ting</a></td><td align="center">67</td><td align="center">0</td><td align="center">67</td><td align="center">0</td><td align="center">&nbsp;</td>
 *    </tr>
 *   </tbody>
 *  </table>
 * </div>
 *}
 *</pre>
 * <br>
 * The code 
 * <pre>
 * {@code
 * List<?> items = (List<?>) page.getByXPath("//div[@class='course']");
 * }
 * </pre>
 * extracts all result-row and stores the corresponding HTML elements to a list called items. Later in the loop it extracts the anchor tag 
 * &lsaquo; a &rsaquo; to retrieve the display text (by .asText()) and the link (by .getHrefAttribute()).   
 * 
 *
 */
public class Scraper {
	private WebClient client;

	/**
	 * Default Constructor 
	 */
	public Scraper() {
		client = new WebClient();
		client.getOptions().setPrintContentOnFailingStatusCode(false);
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
	}

	private void addSlot(HtmlElement e, Section section, Course c, boolean secondRow) {
		String[] date_times = e.getChildNodes().get(secondRow ? 0 : 3).asText().split("\n");
		String times[] =  date_times[date_times.length-1].split(" ");
		String venue = e.getChildNodes().get(secondRow ? 1 : 4).asText();
		String instructors = e.getChildNodes().get(secondRow ? 2 : 5).asText();
		//add instructor to sections
		String instructorString = new String();
		
		if(instructors != null && !instructors.equals("TBA")) {
			for(String instructor : instructors.split("\n")) {
				c.addInstructor(instructor);
				instructorString += instructor;
			}
			section.setInstructor(instructorString);
		}
		if (times[0].equals("TBA"))
			return;

		for (int j = 0; j < times[0].length(); j+=2) {
			String code = times[0].substring(j , j + 2);

			if (Slot.DAYS_MAP.get(code) == null)
				break;
			Slot s = new Slot();
			s.setDay(Slot.DAYS_MAP.get(code));
			s.setStart(times[1]);
			s.setEnd(times[3]);
			s.setVenue(venue);
			section.addSlot(s);

			if(s.getDay() == 1) {
				LocalTime time = LocalTime.parse("15:10:00");
				if( time.compareTo(s.getStart()) >= 0  && time.compareTo(s.getEnd()) <= 0 )
					if(instructors != null && !instructors.equals("TBA"))
						for(String instructor : instructors.split("\n"))
							c.addFilterInstructor(instructor);			
			}
		}

	}

	public String[] scrapeSubject(String baseurl, String term) {
		
		try {
			HtmlPage page = client.getPage(baseurl + "/" + term + "/");
			String url[] = page.getBaseURL().getPath().split("/");
			if(!url[url.length-1].equals(term)) throw new Exception();	
			List<?> items = (List<?>) page.getByXPath("//div[@class='depts']");
			Vector<String> result = new Vector<String>();
			HtmlElement htmlItem = (HtmlElement) items.get(0);
			List<?> subjectList = (List<?>) htmlItem.getByXPath(".//a");
			return ((List<HtmlElement>)subjectList).stream().map( e -> e.getChildNodes().get(0).asText()).toArray(String[]::new);
		}		
		catch (Exception e) {
			return null;
		}
		finally{
			client.close();
		}

	}

	public List<Course> scrape(String baseurl, String term, String sub) {

		try {
			
			HtmlPage page = client.getPage(baseurl + "/" + term + "/subject/" + sub);
			List<?> items = (List<?>) page.getByXPath("//div[@class='course']");			
			Vector<Course> result = new Vector<Course>();

			for (int i = 0; i < items.size(); i++) {
				Course c = new Course();
				HtmlElement htmlItem = (HtmlElement) items.get(i);				
				HtmlElement title = (HtmlElement) htmlItem.getFirstByXPath(".//h2");

				c.setTitle(title.asText());
				
				List<?> popupdetailslist = (List<?>) htmlItem.getByXPath(".//div[@class='popupdetail']/table/tbody/tr");
				HtmlElement exclusion = null;
				for ( HtmlElement e : (List<HtmlElement>)popupdetailslist) {
					HtmlElement t = (HtmlElement) e.getFirstByXPath(".//th");
					HtmlElement d = (HtmlElement) e.getFirstByXPath(".//td");
					if (t.asText().equals("EXCLUSION")) {
						exclusion = d;
					}
				}
				c.setExclusion((exclusion == null ? "null" : exclusion.asText()));
				
				//try to collect information of is the course common core.
				List<?> popupdetailslist_CC = (List<?>) htmlItem.getByXPath(".//div[@class='popupdetail']");
				boolean IsCC = false;
				for (HtmlElement e : (List<HtmlElement>)popupdetailslist_CC) {
					if (e.asText().equals("CC for 4Y 2012 and after")) {
						IsCC = true;
					}
				}	
				c.setIsCC(IsCC);	
				//cc collection done.
				
				List<?> sections = (List<?>) htmlItem.getByXPath(".//tr[contains(@class,'newsect')]");
				for ( HtmlElement e: (List<HtmlElement>)sections) {
					String s = e.getChildNodes().get(1).asText();
					Section section = new Section(s);
					addSlot(e, section, c, false);
					e = (HtmlElement)e.getNextSibling();
					if (e != null && !e.getAttribute("class").contains("newsect"))
						addSlot(e, section, c, true);
					c.addSection(section);
				}
				
				result.add(c);
			}
			client.close();
			return result;

		} catch (Exception e) {
			return null;
		}
		finally {
			client.close();
		}

	}
	
	private HashMap<String, Double> _courseLookUpTable = new HashMap<String, Double>();
	private HashMap<String, Instructor> _instructorSFQTable = new HashMap<String, Instructor>();
	
	public void scrapeSFQ(String baseurl) throws Exception {
		baseurl = "file:///C:\\Users\\xdlok\\Documents\\GitHub\\comp3111-project-2020s\\src\\main\\resources\\sfq.html";
		// Reset tables
		_courseLookUpTable = new HashMap<String, Double>();
		_instructorSFQTable = new HashMap<String, Instructor> ();
		try {
			HtmlPage page = client.getPage(baseurl);
			List<?> tables = (List<?>) page.getByXPath("//table");
			// The first two and the last tables are junk
			List<?> subjects = tables.subList(2, tables.size()-1);
			for(HtmlElement subject: (List<HtmlElement>) subjects) {
				scrapeSFQSubject(subject);
			}
		} catch(Exception e) {
			throw e;
		}finally {
			client.close();
		}
	}
	
	private void scrapeSFQSubject(HtmlElement subject) {
		// Initialize
		String currentCourse = null;
		double currentSum = 0d;
		int numSection = 0;
		List<?> tableEntry = (List<?>) subject.getByXPath(".//tr");
		// Get rid of the head and the tail (with Department Overall)
		tableEntry = tableEntry.subList(1, tableEntry.size()-1);
		for(HtmlElement tr: (List<HtmlElement>) tableEntry) {
			//System.out.println(tr.asXml());
			List<?> td = (List<?>) tr.getByXPath(".//td");
			if(isNewCourse(td)) {
				// Add entry to table
				if(currentCourse != null) {
					double average = currentSum / numSection;
					_courseLookUpTable.put(currentCourse, average);
				}
				
				// Sets new course, and remove white space
				// e.g. COMP 1021 becomes COMP1021
				currentCourse = ((HtmlElement) td.get(0)).asText().replaceAll("\\s+","");
				// Reset sum
				currentSum = 0d;
				numSection = 0;
			}else if(isSession(td)) {
				// Get SFQ score
				String SFQBlock = ((HtmlElement) td.get(3)).asText();
				String sectionScore = SFQBlock.split("\\(")[0];
				// If the score is not -, update sum and num sesction counted
				if(!sectionScore.equals("-")) {
					double score = Double.parseDouble(sectionScore);
					currentSum += score;
					numSection++;
				}
			}else if(isInstructor(td)) { // Instructor column
				String instructorName = ((HtmlElement) td.get(2)).asText();
				// Grab instructor entry, or create one of not yet created
				_instructorSFQTable.putIfAbsent(instructorName, new Instructor(instructorName));
				Instructor instructor = _instructorSFQTable.get(instructorName);
				// Get SFQ score
				String SFQBlock = ((HtmlElement) td.get(4)).asText();
				String instructorScore = SFQBlock.split("\\(")[0];
				// If the score is not -, update instructor score and put it back
				if(!instructorScore.equals("-")) {
					double score = Double.parseDouble(instructorScore);
					instructor.addScore(score);
					}
				_instructorSFQTable.replace(instructorName, instructor);
			} // Else, empty instructor name, do nothing
		}
	}
	
	private boolean isNewCourse(List<?> entry) {
		HtmlElement firstEntry = (HtmlElement) entry.get(0);
		String colspan = firstEntry.getAttribute("colspan");
		return colspan.equals("3");
	}
	
	private boolean isSession(List<?> entry) {
		HtmlElement elem = (HtmlElement) entry.get(1);
		return !elem.asText().equals(" ");
	}
	
	private boolean isInstructor(List<?> entry) {
		HtmlElement elem = (HtmlElement) entry.get(2);
		return !elem.asText().equals("  ");
	}
	

	public double SFQLookUp(Course course){
		if(_courseLookUpTable.isEmpty())
			return Double.NaN;
		if(_courseLookUpTable.containsKey(course.getCourseCode())) {
			return _courseLookUpTable.get(course.getCourseCode());
			}
		return Double.NaN;
	}

	public List<Instructor> scrapeSFQInstructor(String baseurl) throws Exception{
		scrapeSFQ(baseurl);
		return new ArrayList<Instructor>(_instructorSFQTable.values());
	}
	
	
}
