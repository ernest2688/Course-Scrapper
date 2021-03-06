package comp3111.coursescraper;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import java.io.File;
import java.util.List;
import java.util.Vector;

import comp3111.coursescraper.Scraper;
import comp3111.coursescraper.Course;

public class CourseScraperTest {
	
	private String url, term, subject;
	private final String baseurl = "file:/" + (new File("src/main/resources").getAbsolutePath());
	private Scraper scraper = new Scraper();
	private List<Course> courses;

	@Test
	public void testSearchCourseNormal() throws Exception {
		final String title = "ACCT 1010 - Accounting, Business and Society (3 units)";
		Course course = null;
		String result;
		term = "1940";
		subject = "ACCT";
		courses = scraper.scrape(baseurl,term,subject);
		while(true){
			if(courses!=null){
				for(Course c: courses) {
					if(c.getTitle().equals(title))
						course = c;
				}
				assertNotNull(course);
				return;
			}
		}
	}
	
	@Test
	public void testSearchCourseCountNormal() throws Exception {
		int numberOfScrapedCourse;
		final int expectedNumber = 6;
		term = "1940";
		subject = "COMP";
		courses = scraper.scrape(baseurl,term,subject);
		while(true){
			if(courses!=null){
				numberOfScrapedCourse = courses.size();
				assertEquals(numberOfScrapedCourse, expectedNumber);
				return;
			}
		}
	}
	
	@Test
	public void testSearchCourseInvalidTerm() throws Exception {
		term = "1810";
		subject = "ACCT";
		courses = scraper.scrape(baseurl,term,subject);
		assertNull(courses);
	}

	@Test
	public void testSearchCourseInvalidSubject() throws Exception {
		term = "1920";
		subject = "TEST";
		courses = scraper.scrape(baseurl,term,subject);
		assertNull(courses);
	}

}
