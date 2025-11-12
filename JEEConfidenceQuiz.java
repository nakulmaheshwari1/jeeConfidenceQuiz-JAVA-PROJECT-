
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

// Base User class : this class stores basic user information required for registration

class User {
    private String name, email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}

// Student
class Student extends User {

    private int age, totalScore;

    private List<Subject> subjects;

    public Student(String name, String email, int age) {

        super(name, email);
        this.age = age;
        totalScore = 0;
        subjects = new ArrayList<>();
    }

    public int getAge() {
        return age;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void addScore(int s) {
        totalScore += s;
    }

    public void addSubject(Subject s) {
        subjects.add(s);
    }

    public List<Subject> getSubjects() {
        return subjects;
    }
}

// Abstract Question: this class define the basic structure of a question

abstract class Question {

    protected String text;
    protected String[] options;
    protected int correctAnswer;
    protected int subtopicIndex;

    public Question(String text, String[] options, int correctAnswer, int subtopicIndex) {

        this.text = text;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.subtopicIndex = subtopicIndex;
    }

    public String getText() {
        return text;
    }

    public String[] getOptions() {
        return options;
    }

    public int getSubtopicIndex() {
        return subtopicIndex;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public abstract int checkAnswer(int selected, int confidenceValue);
}

// MCQ Question: this class extends Question to implement MCQ specific Logic

class MCQQuestion extends Question {

    public MCQQuestion(String text, String[] options, int correctAnswer, int subtopicIndex) {
        super(text, options, correctAnswer, subtopicIndex);
    }

    @Override
    public int checkAnswer(int selected, int confidenceValue) {
        int base = (selected == correctAnswer) ? 4 : -1;
        return base * confidenceValue;
    }

    public boolean isCorrect(int selected) {
        return selected == correctAnswer;
    }
}

// Subtopic : this class represents a subtopic containing multiple questions as
// well as score and confidence tracking

class Subtopic {
    private String name;
    private List<MCQQuestion> questions;
    private int score, totalConfidence, totalMaxConfidence;

    public Subtopic(String name) {
        this.name = name;
        questions = new

        // arraylist is used to store questions of each subtopic

        ArrayList<>();
        score = 0;
        totalConfidence = 0;
        totalMaxConfidence = 0;
    }

    public void addQuestion(MCQQuestion q) {
        questions.add(q);
    }

    public List<MCQQuestion> getQuestions() {
        return questions;
    }

    public String getName() {
        return name;
    }

    public void processAnswer(MCQQuestion q, int selected, int confidenceValue) {

        // conditional operator is used to check if the selected answer is correct or
        // not. marks are updated accordingly.

        score += (selected == q.getCorrectAnswer()) ? 4 : -1;
        totalConfidence += (selected == q.getCorrectAnswer()) ? confidenceValue : 0;
        totalMaxConfidence += 3; // max confidence per question = 3
    }

    public int getScore() {
        return score;
    }

    public double getConfidencePercentage() {
        return totalMaxConfidence == 0 ? 0 : ((double) totalConfidence / totalMaxConfidence) * 100;
    }
}

// Subject : this class represents a subject containing multiple subtopics.
// Object of this class is used to group subtopics under a subject.

class Subject {
    private String name;
    private List<Subtopic> subtopics;

    public Subject(String name) {
        this.name = name;
        subtopics = new ArrayList<>();
    }

    public void addSubtopic(Subtopic s) {
        subtopics.add(s);
    }

    public List<Subtopic> getSubtopics() {
        return subtopics;
    }

    public String getName() {
        return name;
    }

    public int getTotalScore() {
        int s = 0;
        for (Subtopic st : subtopics)
            s += st.getScore();
        return s;
    }
}

// Quiz : this class manages the quiz flow, current question, and tracks
// progress through subjects and subtopics

class Quiz {
    public int subjectIndex = 0, subtopicIndex = 0, questionIndex = 0;
    public Subject currentSubject;
    public Subtopic currentSubtopic;
    public List<MCQQuestion> currentQuestions;
    private Student student;
    private boolean finished = false;

    public Quiz(Student student) {
        this.student = student;
    }

    public Student getStudent() {
        return student;
    }

    public void start() {
        subjectIndex = 0;
        subtopicIndex = 0;
        questionIndex = 0;
        finished = false;
        if (student.getSubjects().isEmpty()) {
            finished = true;
            return;
        }
        currentSubject = student.getSubjects().get(subjectIndex);
        currentSubtopic = currentSubject.getSubtopics().get(subtopicIndex);
        currentQuestions = currentSubtopic.getQuestions();
    }

    public MCQQuestion getCurrentQuestion() {
        if (finished)
            return null;
        if (currentQuestions == null || questionIndex >= currentQuestions.size())
            return null;
        return currentQuestions.get(questionIndex);
    }

    public void submitAnswer(int selected, int confidenceValue) {
        MCQQuestion q = getCurrentQuestion();
        if (q == null)
            return;
        int gained = q.checkAnswer(selected, confidenceValue);

        // Update subtopic and student's total score

        currentSubtopic.processAnswer(q, selected, confidenceValue);
        student.addScore(gained);

        // following logic is used to move to next question, subtopic or subject based
        // on current indices

        questionIndex++;
        if (questionIndex >= currentQuestions.size()) {
            questionIndex = 0;
            subtopicIndex++;
            if (subtopicIndex >= currentSubject.getSubtopics().size()) {
                subtopicIndex = 0;
                subjectIndex++;
                if (subjectIndex >= student.getSubjects().size()) {
                    finished = true;
                    return;
                } else {
                    currentSubject = student.getSubjects().get(subjectIndex);
                }
            }
            currentSubtopic = currentSubject.getSubtopics().get(subtopicIndex);
            currentQuestions = currentSubtopic.getQuestions();
        }
    }

    public boolean isQuizFinished() {
        return finished;
    }
}

// GUI : This class implements the Java Swing GUI for registration and quiz
// interface
// java swing is used to create the GUI for the quiz application.
// It includes registration panel, quiz panel, question display, options
// selection, confidence level selection, and progress tracking. also shows
// final results.
// It handles user interactions and updates the interface accordingly.
// The radio buttons are added by using ButtonGroup to ensure only one option
// can be selected at a time.
// the progress bar is added by
// the final results are displayed in a JTextArea within a JScrollPane for
// better readability.
// We are not experienced

public class JEEConfidenceQuiz extends JFrame implements ActionListener {

    private Student student;
    private Quiz quiz;
    private JPanel regPanel, quizPanel;
    private JTextField nameField, ageField, emailField;
    private JButton regButton, nextButton;
    private JLabel subjectLabel, questionLabel, progressLabel;
    private JRadioButton[] options;
    private ButtonGroup optionGroup;
    private JRadioButton highConf, mediumConf, lowConf;
    private ButtonGroup confGroup;
    private JProgressBar progressBar;

    public JEEConfidenceQuiz() {
        createRegistrationGUI();
        setTitle("JEE Confidence Quiz");
        setSize(820, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createRegistrationGUI() {
        regPanel = new JPanel(new GridBagLayout());
        regPanel.setBackground(new Color(230, 240, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("JEE Confidence Quiz");
        title.setFont(new Font("SansSerif", Font.BOLD, 60));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        regPanel.add(title, gbc);
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        regPanel.add(new JLabel("Name:"), gbc);
        nameField = new JTextField();
        gbc.gridx = 1;
        regPanel.add(nameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        regPanel.add(new JLabel("Age:"), gbc);
        ageField = new JTextField();
        gbc.gridx = 1;
        regPanel.add(ageField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        regPanel.add(new JLabel("Email:"), gbc);
        emailField = new JTextField();
        gbc.gridx = 1;
        regPanel.add(emailField, gbc);

        regButton = new JButton("Register & Start Quiz");
        regButton.addActionListener(e -> registerStudent());
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        regPanel.add(regButton, gbc);

        add(regPanel);
    }

    // method to register student and initialize quiz data
    // catches exceptions for invalid inputs using try catch block

    private void registerStudent() {
        try {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            if (name.isEmpty() || email.isEmpty())
                throw new Exception("Empty fields");
            student = new Student(name, email, age);
            initializeQuizData();
            quiz = new Quiz(student);
            remove(regPanel);
            setupQuizGUI();
            quiz.start();
            displayQuestion();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input: provide valid name, age (integer) and email.");
        }
    }

    // method to setup the quiz GUI components
    // uses BorderLayout and GridBagLayout for arranging components
    // adds action listener to next button to handle answer submission

    private void setupQuizGUI() {
        quizPanel = new JPanel(new BorderLayout(10, 10));
        quizPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        quizPanel.setBackground(new Color(250, 250, 255));

        // Header: Indicates current subject and progress bar

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        subjectLabel = new JLabel();
        subjectLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        subjectLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        header.add(subjectLabel, BorderLayout.WEST);

        // Progress Bar: shows quiz progress
        // progress bar maximum is total questions (subjects * subtopics * questions)
        // it works on approximate basis to give visual feedback

        progressBar = new JProgressBar(0, student.getSubjects().size() * 4 * 7);

        progressBar.setStringPainted(true);
        header.add(progressBar, BorderLayout.EAST);
        quizPanel.add(header, BorderLayout.NORTH);

        // Center: Question display, options, confidence selection
        // uses GridBagLayout for flexible arrangement
        // We are not experienced in GUI so we used online resources to learn about
        // GridBagLayout and other Swing components and may not have complete knowledge
        // of all best practices.

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 25));
        questionLabel.setVerticalAlignment(SwingConstants.TOP);
        center.add(questionLabel, gbc);

        // Options: Radio buttons for answer choices (just liKe in HTML)

        options = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        gbc.gridwidth = 1;
        for (int i = 0; i < 4; i++) {
            gbc.gridy = i + 1;
            gbc.gridx = 0;
            options[i] = new JRadioButton();
            options[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            optionGroup.add(options[i]);
            center.add(options[i], gbc);
        }

        // JPanel for confidence level selection

        JPanel confPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        confPanel.setOpaque(false);
        confPanel.add(new JLabel("Confidence: "));
        highConf = new JRadioButton("High");
        mediumConf = new JRadioButton("Medium");
        lowConf = new JRadioButton("Low");
        highConf.setFont(new Font("SansSerif", Font.PLAIN, 20));
        mediumConf.setFont(new Font("SansSerif", Font.PLAIN, 20));
        lowConf.setFont(new Font("SansSerif", Font.PLAIN, 20));
        confGroup = new ButtonGroup();
        confGroup.add(highConf);
        confGroup.add(mediumConf);
        confGroup.add(lowConf);
        confPanel.add(highConf);
        confPanel.add(mediumConf);
        confPanel.add(lowConf);
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        center.add(confPanel, gbc);

        progressLabel = new JLabel();
        progressLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridy = 6;
        center.add(progressLabel, gbc);

        quizPanel.add(center, BorderLayout.CENTER);

        nextButton = new JButton("Next");
        nextButton.addActionListener(this);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        south.add(nextButton);
        quizPanel.add(south, BorderLayout.SOUTH);

        add(quizPanel);
        revalidate();
        repaint();
    }

    private void updateProgressBar() {
        // int answered = student.getTotalScore();
        // progress bar uses total points possible (approx); to give a visual, we'll
        // compute number of answered questions by counting subtopic maxConfidence
        int totalQuestions = student.getSubjects().size() * 3 * 4;
        // find how many questions answered by summing processed questions
        // (totalMaxConfidence/3)
        // int answeredCount=0;
        // for(Subject subj: student.getSubjects()){
        // for(Subtopic st: subj.getSubtopics()){
        // answeredCount += st.getConfidencePercentage()==0?0:
        // (int)Math.round(st.getConfidencePercentage()/100.0 *
        // (st.getQuestions().size()));
        // }
        // }
        // fallback — just compute based on quiz indices
        int approxAnswered = quiz.subjectIndex * 3 * 4 + quiz.subtopicIndex * 4 + quiz.questionIndex;
        progressBar.setMaximum(totalQuestions);
        progressBar.setValue(Math.min(totalQuestions, approxAnswered));
        progressBar.setString(
                "Progress: " + Math.min(totalQuestions, approxAnswered) + " / " + totalQuestions + " questions");
    }

    private void displayQuestion() {
        MCQQuestion q = quiz.getCurrentQuestion();
        if (q == null) {
            if (quiz.isQuizFinished()) {
                showResults();
                return;
            } else {

                // no current question but not finished — try to advance
                showResults();
                return;
            }
        }
        subjectLabel.setText("Subject: " + student.getSubjects().get(quiz.subjectIndex).getName());
        questionLabel.setText("<html><body style='width:600px'>Q: " + q.getText() + "</body></html>");
        String[] opts = q.getOptions();
        for (int i = 0; i < 4; i++)
            options[i].setText(opts[i]);
        optionGroup.clearSelection();
        confGroup.clearSelection();
        progressLabel.setText("Question " + (quiz.questionIndex + 1) + " of " + quiz.currentQuestions.size()
                + " (Subtopic: " + quiz.currentSubtopic.getName() + ")");
        updateProgressBar();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int selected = -1;
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                selected = i;
                break;
            }
        }
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Select an answer!");
            return;
        }
        int confVal = highConf.isSelected() ? 3 : (mediumConf.isSelected() ? 2 : (lowConf.isSelected() ? 1 : 0));
        if (confVal == 0) {
            JOptionPane.showMessageDialog(this, "Select your confidence level!");
            return;
        }
        quiz.submitAnswer(selected, confVal);
        displayQuestion();
    }

    private void showResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("******** FINAL RESULT ********\nTotal Score: " + student.getTotalScore() + "\n\n");
        for (Subject subj : student.getSubjects()) {
            sb.append(subj.getName() + ": " + subj.getTotalScore() + "\n");
            for (Subtopic st : subj.getSubtopics()) {
                sb.append("  " + st.getName() + " Score:" + st.getScore() + " Conf:"
                        + String.format("%.2f", st.getConfidencePercentage()) + "%\n");
                if (st.getConfidencePercentage() >= 75)
                    sb.append("    Feedback: You are good to go!\n");
                else if (st.getConfidencePercentage() >= 50)
                    sb.append("    Feedback: Revise concepts.\n");
                else
                    sb.append("    Feedback: Work harder!\n");
            }
            sb.append("\n");
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(this, sp, "Result", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    private void initializeQuizData() {
        String[] subjects = { "Physics", "Chemistry", "Maths" };
        String[][] subtopics = { { "Mechanics", "Fluids", "Electromagnetism" }, { "Physical", "Organic", "Inorganic" },
                { "Algebra", "Calculus", "Geometry" } };

        // questions[subject][subtopic][question][fields] : We Hardcoded questions for
        // demo purposes and easy initialization rather than loading from external files
        // or DB.

        String[][][][] questions = new String[3][3][4][6];

        // Physics - Mechanics
        questions[0][0] = new String[][] {
                { "A 2kg block is pushed with 10N. Acceleration?", "2", "5", "10", "20", "0" },

                { "Free fall from 20m. Velocity after 2s?", "10", "15", "20", "25", "0" },

                { "Force on 5kg at 2m/s²?", "5", "10", "15", "20", "0" },

                { "Mass 3kg, F=12N. Acceleration?", "2", "3", "4", "6", "1" }
        };
        // Physics - Fluids
        questions[0][1] = new String[][] {
                { "Fluid density effect on pressure?", "Direct", "Inverse", "None", "Cannot say", "0" },

                { "Pascal principle example?", "Hydraulic lift", "Boiling", "Magnet", "Spring", "0" },

                { "Archimedes principle?", "Upthrust", "Friction", "Pressure", "Velocity", "0" },

                { "Viscosity increases?", "Speed decreases", "Speed increases", "No effect", "Cannot say", "0" }
        };
        // Physics - Electromagnetism
        questions[0][2] = new String[][] {
                { "EMF across 5Ω resistor with 10A?", "50V", "10V", "5V", "20V", "0" },

                { "Magnetic force direction?", "Perpendicular", "Parallel", "Opposite", "Along field", "0" },

                { "Lenz law example?", "Induced current", "Resistance", "Velocity", "Acceleration", "0" },

                { "Faraday law unit?", "Volt", "Ampere", "Newton", "Joule", "0" }
        };

        // Chemistry - Physical
        questions[1][0] = new String[][] {
                { "HCl + NaOH reaction?", "Salt", "Water", "Acid", "Base", "0" },

                { "Organic compound CH4?", "Methane", "Ethanol", "Ethane",
                        "Propane", "0" },

                { "Atomic number of O?", "6", "8", "16", "12", "1" },

                { "Periodic table group of Na?", "Alkali", "Halogen", "Noble", "Transition", "0" }
        };
        // Chemistry - Organic
        questions[1][1] = new String[][] {
                { "Alkane formula C2H6?", "Ethane", "Methane", "Propane", "Butane", "0" },

                { "pH of neutral solution?", "7", "0", "14", "1", "0" },

                { "Redox example?", "Zn+CuSO4", "H2O", "CO2", "NaCl", "0" },

                { "Balancing H2+O2?", "H2O", "H2O2", "OH", "H2", "0" }
        };
        // Chemistry - Inorganic
        questions[1][2] = new String[][] {
                { "Which is amphoteric?", "Aluminium", "Sodium", "Chlorine", "Helium", "0" },

                { "Most electronegative element?", "Fluorine", "Oxygen", "Chlorine", "Nitrogen", "0" },

                { "Ionic bond formed by?", "Transfer of electrons", "Sharing of electrons", "No electrons", "Both",
                        "0" },

                { "Common salt name?", "Sodium Chloride", "Sodium Oxide", "Potassium Chloride", "Calcium Carbonate",
                        "0" }
        };

        // Maths - Algebra
        questions[2][0] = new String[][] {
                { "Solve x+2=5", "2", "3", "5", "4", "1" },

                { "Derivative of x²?", "2x", "x²", "x", "1", "0" },

                { "Integral of 2x?", "x²", "2x²", "x", "0", "0" },

                { "Sum of 1+2+3?", "3", "6", "10", "1", "1" }
        };
        // Maths - Calculus
        questions[2][1] = new String[][] {
                { "Determinant of [[1,2],[3,4]]?", "-2", "2", "0", "1", "0" },

                { "Solve y=2x+3, y when x=2?", "7", "5", "6", "8", "0" },

                { "Area of circle r=1?", "3.14", "6.28", "1", "0", "0" },

                { "Slope of line through (0,0) and (2,4)?", "2", "0", "1", "4", "0" }
        };
        // Maths - Geometry
        questions[2][2] = new String[][] {
                { "Angle sum of triangle?", "180", "90", "360", "270", "0" },

                { "Right triangle Pythagoras?", "a^2+b^2=c^2", "a+b=c", "ab=c", "None", "0" },

                { "Area of square side 2?", "4", "8", "2", "16", "0" },

                { "Perimeter of circle?", "2πr", "πr^2", "πd", "πr", "0" }
        };

        // Load questions into student object

        for (int s = 0; s < subjects.length; s++) {
            Subject subj = new Subject(subjects[s]);
            for (int t = 0; t < 3; t++) {
                Subtopic st = new Subtopic(subtopics[s][t]);
                for (int q = 0; q < 4; q++) {
                    String[] qdata = questions[s][t][q];
                    String[] opts = { qdata[1], qdata[2], qdata[3], qdata[4] };
                    int correct = Integer.parseInt(qdata[5]);
                    MCQQuestion mcq = new MCQQuestion(qdata[0], opts, correct, t);
                    st.addQuestion(mcq);
                }
                subj.addSubtopic(st);
            }
            student.addSubject(subj);
        }
    }

    // Main method to launch the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JEEConfidenceQuiz());
    }
}
