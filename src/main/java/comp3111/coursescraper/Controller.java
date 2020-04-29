package comp3111.coursescraper;

import java.awt.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType; 
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;

import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.control.CheckBox;

import java.util.Random;
import java.util.List;
import java.util.Vector;
import java.util.Collections;

import java.time.LocalTime;

public class Controller {

    private String[] subjects;

    private String[] consoleText = new String[TabLabel.values().length];

    private Scraper scraper = new Scraper();

    enum TabLabel {
        Main,
        BackEnd,
        Filter,
        Timetable,
        AllSubject,
        SFQ
    }

    @FXML
    private Tab tabMain;

    @FXML
    private TextField textfieldTerm;

    @FXML
    private TextField textfieldSubject;

    @FXML
    private Button buttonSearch;

    @FXML
    private TextField textfieldURL;

    @FXML
    private Tab tabStatistic;

    @FXML
    private ComboBox<?> comboboxTimeSlot;

    @FXML
    private Tab tabFilter;

    @FXML
    private Tab tabList;

    @FXML
    private Tab tabTimetable;

    @FXML
    private Tab tabAllSubject;

    @FXML
    private ProgressBar progressbar;

    @FXML
    private TextField textfieldSfqUrl;

    @FXML
    private Button buttonSfqEnrollCourse;

    @FXML
    private Button buttonInstructorSfq;

    @FXML
    private TextArea textAreaConsole;
//<<<<<<< ernest_dev
    
//     @FXML
//     private Button buttonSelectAll;
    
//     @FXML
//     private CheckBox checkboxAM;
    
//     @FXML
//     private CheckBox checkboxPM;
    
//     @FXML
//     private CheckBox checkboxMon;
    
//     @FXML
//     private CheckBox checkboxTue;
    
//     @FXML
//     private CheckBox checkboxWed;
    
//     @FXML
//     private CheckBox checkboxThu;
    
//     @FXML
//     private CheckBox checkboxFri;
    
//     @FXML
//     private CheckBox checkboxSat;
    
//     @FXML
//     private CheckBox checkboxCC;
    
//     @FXML
//     private CheckBox checkboxNoEx;
    
//     @FXML
//     private CheckBox checkboxWLoT;
    
    
    
//     private Scraper scraper = new Scraper();
    
//     private List<Course> courses;
//     LocalTime noon = LocalTime.parse("12:00:00");
    
// =======

// >>>>>>> master
    @FXML
    void allSubjectSearch() {
        new Thread(() -> {
            if(!subjectIsSearched(true)) return;
            progressbar.setProgress(0);
            final double increment = 1.0 / subjects.length;
            int counted_course = 0;
            for (String subject : subjects) {
                counted_course += searchCourse(subject);
                System.out.println(subject + " is done");
                Platform.runLater( () -> progressbar.setProgress(progressbar.getProgress() + increment) );
                try {Thread.sleep(100);} catch (Exception e) {}
            }
            progressbar.setProgress(1);

            String newline = "Total Number of Courses fetched:\n";
            newline += Integer.toString(counted_course) + "\n";
            consoleText[TabLabel.AllSubject.ordinal()] = newline;
            textAreaConsole.setText(textAreaConsole.getText() + "\n" + newline);
        }).start();
    }

    @FXML
    void findInstructorSfq() {
    	buttonInstructorSfq.setDisable(true);
    }

    @FXML
    void findSfqEnrollCourse() {

    }

    @FXML
    void search() {

        new Thread(() -> {
            subjectIsSearched(false);
            if(subjects != null)
                searchCourse(textfieldSubject.getText());
        }).start();

    	//Add a random block on Saturday
    	AnchorPane ap = (AnchorPane)tabTimetable.getContent();
    	Label randomLabel = new Label("COMP1022\nL1");
    	Random r = new Random();
    	double start = (r.nextInt(10) + 1) * 20 + 40;

    	randomLabel.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
    	randomLabel.setLayoutX(600.0);
    	randomLabel.setLayoutY(start);
    	randomLabel.setMinWidth(100.0);
    	randomLabel.setMaxWidth(100.0);
    	randomLabel.setMinHeight(60);
    	randomLabel.setMaxHeight(60);
    
    	ap.getChildren().addAll(randomLabel);
    	 	
    	
    }

    private Boolean subjectIsSearched(Boolean allSubject) {
        if (subjects != null) return true;
        
        subjects = scraper.scrapeSubject(textfieldURL.getText(), textfieldTerm.getText());
        if(subjects == null){
            Platform.runLater( () -> {
                final Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText(null);
                alert.setContentText("404 NOT FOUND! \n Please Check the Base URL and Term.");
                alert.showAndWait();
            });
            return false;
        }

        String newline = "Total Number of Categories/Code Prefix: " + Integer.toString(subjects.length) + "\n";
        consoleText[TabLabel.AllSubject.ordinal()] = newline;
        if(allSubject)
            textAreaConsole.setText(textAreaConsole.getText() + "\n" + newline);
        return false;
        
    }

    private int searchCourse(String subject){
        
    	List<Course> courses = scraper.scrape(textfieldURL.getText(), textfieldTerm.getText(), subject);

        if(courses == null) {
            Platform.runLater( () -> {
                final Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText(null);
                alert.setContentText("404 NOT FOUND! \n Please Check the Base URL, Term and Subject.");
                alert.showAndWait();
            });
            return 0;
        }

        int NUMBER_OF_SECTIONS = 0, NUMBER_OF_COURSES = 0;

        Vector<String> INSTRUCTOR_NAME = new Vector<String>();

        courses.forEach( c -> {
            c.getInstructor().forEach( instructor -> {
                if(!INSTRUCTOR_NAME.contains(instructor))
                    INSTRUCTOR_NAME.add(instructor);
            });
        } );

    	for (Course c : courses) {
            String courseTitle = c.getTitle() + "\n";
    		String newline = new String();
            Boolean counted = false;
            for(String instructor : c.getFilterInstructor())
                if(INSTRUCTOR_NAME.contains(instructor))
                    INSTRUCTOR_NAME.remove(instructor);
            Collections.sort(INSTRUCTOR_NAME);
    		for (Section section : c.getSection()) {
                if(section != null){
                    if(!counted && section.getType() != null){
                        counted = true;
                        courseTitle += newline;
                        newline = courseTitle;
                        ++NUMBER_OF_COURSES;
                    }
                    ++NUMBER_OF_SECTIONS;
                    int i = 0;
                    for(Slot t : section.getSlot())
                        if(t != null)
                            newline += section + " Slot " + i++ + ":" + t + "\n";
                }
    		}
            if(counted)
    		    textAreaConsole.setText(textAreaConsole.getText() + "\n" + newline);
            try {Thread.sleep(100);} catch (Exception e) {}
    	}

        String newline = "Total Number of difference sections : " + Integer.toString(NUMBER_OF_SECTIONS) + "\n";
        newline += "Total Number of Course : " + Integer.toString(NUMBER_OF_COURSES) + "\n";
        newline += "Instructors who has teaching assignment this term but does not need to teach at Tu 3:10pm : ";
        
        for(String instructor : INSTRUCTOR_NAME)
            newline += instructor + ", ";
        newline = newline.substring(0, newline.length() - 2);
        newline += "\n";

        consoleText[TabLabel.BackEnd.ordinal()] = newline;

        consoleText[TabLabel.Main.ordinal()] = textAreaConsole.getText();

        return courses.size();
    }
    
    @FXML
	void SelectAll(){
    	if(buttonSelectAll.getText().equals("Select All")) {
    		buttonSelectAll.setText("De-select All");
    		checkboxAM.setSelected(true);
    		checkboxPM.setSelected(true);
    		checkboxMon.setSelected(true);
    		checkboxTue.setSelected(true);
    		checkboxWed.setSelected(true);
    		checkboxThu.setSelected(true);
    		checkboxFri.setSelected(true);
    		checkboxSat.setSelected(true);
    		checkboxCC.setSelected(true);
    		checkboxNoEx.setSelected(true);
    		checkboxWLoT.setSelected(true);
    		filter();
    
    	}
    	
    	else if(buttonSelectAll.getText().equals("De-select All")) {
    		buttonSelectAll.setText("Select All");
    		checkboxAM.setSelected(false);
    		checkboxPM.setSelected(false);
    		checkboxMon.setSelected(false);
    		checkboxTue.setSelected(false);
    		checkboxWed.setSelected(false);
    		checkboxThu.setSelected(false);
    		checkboxFri.setSelected(false);
    		checkboxSat.setSelected(false);
    		checkboxCC.setSelected(false);
    		checkboxNoEx.setSelected(false);
    		checkboxWLoT.setSelected(false);
    		filter();
    	}
    	
	}
    
    @FXML
    void filter() {
    	textAreaConsole.clear();
    	for (Course c : courses) {
   
    		//String newline = c.getTitle() + "\n";
    		//if the course is not CC and the cc box is checked, skip the course
    		if(checkboxCC.isSelected()) {
    			if(!c.getIsCC()) {
    				continue;
    			}
    		}
    		//if the NoEx is checked while the course have exclusions, skip the course
    		if(checkboxNoEx.isSelected()) {
    			if(!c.getExclusion().equals("null")) {
    				continue;
    			}
    		}
    		
    		boolean isLoRT = true;
    		if(checkboxWLoT.isSelected()) {isLoRT=false;}
    		boolean isAM   = true;
    		if(checkboxAM.isSelected()) {isAM=false;}
    		boolean isPM   = true;
    		if(checkboxPM.isSelected()) {isPM=false;}
    		boolean isAMPM = true;
    		if(checkboxAM.isSelected() && checkboxPM.isSelected()) {isAMPM=false;}
    		boolean isMon  = true;
    		if(checkboxMon.isSelected()) {isMon=false;}
    		boolean isTue  = true;
    		if(checkboxTue.isSelected()) {isTue=false;}
    		boolean isWed  = true;
    		if(checkboxWed.isSelected()) {isWed=false;}
    		boolean isThu  = true;
    		if(checkboxThu.isSelected()) {isThu=false;}
    		boolean isFri  = true;
    		if(checkboxFri.isSelected()) {isFri=false;}
    		boolean isSat  = true;
    		if(checkboxSat.isSelected()) {isSat=false;}
    		for (Section section : c.getSection()) {
                if(section != null){
                	if(checkboxWLoT.isSelected() && !isLoRT) {
                		if(section.getType()=="LAB"||section.getType()=="TUT") 
                			isLoRT = true;
                    }
                    for(Slot t : section.getSlot()) {
                    	if(t!=null) {
                    		if(checkboxAM.isSelected() && !isAM) {
                    			if(noon.compareTo(t.getStart())>0 && noon.compareTo(t.getEnd())>0) 
                    				isAM=true;          			
                    		}
                    		if(checkboxPM.isSelected() && !isPM) {
                    			if(noon.compareTo(t.getStart())<0 && noon.compareTo(t.getEnd())<0) 
                    				isPM=true;          			
                    		}
                    		if(checkboxAM.isSelected() && checkboxPM.isSelected() && !isAMPM) {
                    			if(isAM&&isPM || noon.compareTo(t.getStart())>0 && noon.compareTo(t.getEnd())<0)
                    				isAMPM=true;
                    				         			
                    		}
                    		if(checkboxMon.isSelected() && !isMon) {
                    			if(t.getDay()==0)
                    				isMon=true;
                    		}
                    		if(checkboxTue.isSelected() && !isTue) {
                    			if(t.getDay()==1)
                    				isTue=true;
                    		}
                    		if(checkboxWed.isSelected() && !isWed) {
                    			if(t.getDay()==2)
                    				isWed=true;
                    		}
                    		if(checkboxThu.isSelected() && !isThu) {
                    			if(t.getDay()==3)
                    				isThu=true;
                    		}
                    		if(checkboxFri.isSelected() && !isFri) {
                    			if(t.getDay()==4)
                    				isFri=true;
                    		}
                    		if(checkboxSat.isSelected() && !isSat) {
                    			if(t.getDay()==5)
                    				isSat=true;
                    		}
                    		
                    		
                    		
                    		
                    	}
                    	
                    }
                	
                	
                         
                    
                }
    		}
    		
    		
    	
    		if(!(isLoRT && isAM && isPM && isAMPM && isMon && isTue && isWed && isThu && isFri && isSat)) 
    			continue;
    		
    		String newline = c.getTitle() + "\n";
            
    		for (Section section : c.getSection()) {
                if(section != null){
                    int i = 0;
                    for(Slot t : section.getSlot())
                        if(t != null)
                            newline += section + " Slot " + i++ + ":" + t + "\n";
                }
    		}
    		textAreaConsole.setText(textAreaConsole.getText() + "\n" + newline);
    		
    	}
    	
    }
}

	


