
// DataStore.java
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * CSV storage format (simple):
 * classes.csv -> className,subjectsPipeSeparated,fee
 * students.csv ->
 * id,name,contact,klass,password,feePaid,attendanceEncoded,marksEncoded
 * teachers.csv -> id,name,contact,password,assignedEncoded
 *
 * Encodings:
 * attendanceEncoded = date1~P^date2~A (date format yyyy-MM-dd)
 * marksEncoded = subj1:90^subj2:85
 * assignedEncoded = class~sub1|sub2;class2~sub3|sub4
 */
public class DataStore {
    public static final String DATA_FOLDER = "data" + File.separator;
    public static final String CLASSES_FILE = DATA_FOLDER + "classes.csv";
    public static final String STUDENTS_FILE = DATA_FOLDER + "students.csv";
    public static final String TEACHERS_FILE = DATA_FOLDER + "teachers.csv";
    public static final String RECEIPT_FILE = DATA_FOLDER + "receipt_no.csv";

    public static String adminPassword = "admin";
    public static List<ClassRoom> classes = new ArrayList<>();
    public static List<Student> students = new ArrayList<>();
    public static List<Teacher> teachers = new ArrayList<>();
    public static final String ADMIN_FILE = DATA_FOLDER + "admin.txt";

    public static void loadAdminPassword() {
        ensureFolder();
        File f = new File(ADMIN_FILE);
        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line = br.readLine();
                if (line != null && !line.trim().isEmpty())
                    adminPassword = line.trim();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveAdminPassword() {
        ensureFolder();
        try (PrintWriter pw = new PrintWriter(new FileWriter(ADMIN_FILE))) {
            pw.println(adminPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Models =====
    public static class ClassRoom {
        public String name;
        public List<String> subjects = new ArrayList<>();
        public double fee = 0.0;
        public List<String> studentIds = new ArrayList<>();
        public static int RECEIPT_NO = 1; // Auto increment receipt number

        public ClassRoom(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public static class Student {
        public String id, name, contact, klass, password;
        public double feePaid;
        public LinkedHashMap<String, String> attendance;
        public LinkedHashMap<String, Integer> marks;
        public List<FeeRecord> feeHistory = new ArrayList<>();

        public Student(String id, String name, String contact, String klass, String password) {
            this.id = id;
            this.name = name;
            this.contact = contact;
            this.klass = klass;
            this.password = password;
            this.feePaid = 0;
            this.attendance = new LinkedHashMap<>();
            this.marks = new LinkedHashMap<>();
            this.feeHistory = new ArrayList<>();
        }

        public static class FeeRecord {
            public double amount; // amount paid
            public double classFeeAtTime; // class fee at the time of payment
            public String date; // date of payment
            public String note; // optional note

            public FeeRecord(double amount, double classFeeAtTime, String note) {
                this.amount = amount;
                this.classFeeAtTime = classFeeAtTime;
                this.note = note;
                this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            }
        }

        public static class FeePayment {
            public String date;
            public double amount;

            public FeePayment(String date, double amount) {
                this.date = date;
                this.amount = amount;
            }
        }

        public double getAttendancePercentage() {
            if (attendance.isEmpty())
                return 0.0;
            int present = 0;
            for (String v : attendance.values())
                if ("P".equals(v))
                    present++;
            return present * 100.0 / attendance.size();
        }

        public int getPresentCount() {
            int c = 0;
            for (String v : attendance.values())
                if ("P".equals(v))
                    c++;
            return c;
        }

        public int getTotalAttendanceCount() {
            return attendance.size();
        }
    }

    public static class Teacher {
        public String id;
        public String name;
        public String contact;
        public String password;
        // assignedClassSubjects: className -> list of subjects
        public Map<String, List<String>> assignedClassSubjects = new LinkedHashMap<>();

        public Teacher(String id, String name, String contact, String password) {
            this.id = id;
            this.name = name;
            this.contact = contact;
            this.password = password;
        }

        public int totalAssignedSubjects() {
            int c = 0;
            for (List<String> l : assignedClassSubjects.values())
                c += l.size();
            return c;
        }
    }

    // ===== Load & Save =====
    public static void loadData() {
        ensureFolder();
        classes.clear();
        loadAdminPassword();
        students.clear();
        teachers.clear();
        loadClasses();
        loadStudents();
        loadTeachers();
        // rebuild class student lists
        for (ClassRoom cr : classes)
            cr.studentIds.clear();
        for (Student s : students) {
            ClassRoom c = getClassByName(s.klass);
            if (c != null && !c.studentIds.contains(s.id))
                c.studentIds.add(s.id);
        }
    }

    private static void ensureFolder() {
        File f = new File(DATA_FOLDER);
        if (!f.exists())
            f.mkdirs();
    }

    private static String[] splitCsv(String line, int minCols) {
        List<String> out = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                sb.append(ch);
            } else if (ch == ',' && !inQuotes) {
                out.add(sb.toString());
                sb.setLength(0);
            } else
                sb.append(ch);
        }
        out.add(sb.toString());
        while (out.size() < minCols)
            out.add("");
        return out.toArray(new String[0]);
    }

    private static String unquote(String s) {
        if (s == null)
            return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }

    private static String escapeCsv(String s) {
        if (s == null)
            return "";
        String out = s.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\"") || out.contains("\n"))
            out = "\"" + out + "\"";
        return out;
    }

    private static void loadClasses() {
        File f = new File(CLASSES_FILE);
        if (!f.exists()) {
            // create default classes 1..10
            for (int i = 1; i <= 10; i++) {
                ClassRoom cr = new ClassRoom(String.valueOf(i));
                classes.add(cr);
            }
            saveClasses();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] cols = splitCsv(line, 3);
                String name = unquote(cols[0]);
                ClassRoom cr = new ClassRoom(name);
                if (cols.length > 1) {
                    String subj = unquote(cols[1]);
                    if (!subj.isEmpty()) {
                        String[] ss = subj.split("\\|");
                        for (String s : ss)
                            if (!s.trim().isEmpty())
                                cr.subjects.add(s.trim());
                    }
                }
                if (cols.length > 2) {
                    try {
                        cr.fee = Double.parseDouble(unquote(cols[2]));
                    } catch (Exception e) {
                        cr.fee = 0.0;
                    }
                }
                classes.add(cr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadStudents() {
        File f = new File(STUDENTS_FILE);
        if (!f.exists())
            return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                // we now expect 9 columns:
                // id,name,contact,klass,password,feePaid,attendance,marks,feeHistory
                String[] cols = splitCsv(line, 9);
                if (cols.length < 5)
                    continue;
                String id = unquote(cols[0]);
                String name = unquote(cols[1]);
                String contact = unquote(cols[2]);
                String klass = unquote(cols[3]);
                String password = unquote(cols[4]);
                Student s = new Student(id, name, contact, klass, password);

                if (cols.length > 5 && !unquote(cols[5]).isEmpty()) {
                    try {
                        s.feePaid = Double.parseDouble(unquote(cols[5]));
                    } catch (Exception ex) {
                        s.feePaid = 0.0;
                    }
                }
                if (cols.length > 6 && !unquote(cols[6]).isEmpty()) {
                    String att = unquote(cols[6]);
                    String[] pairs = att.split("\\^");
                    for (String p : pairs) {
                        if (p.isEmpty())
                            continue;
                        String[] kv = p.split("~", 2);
                        if (kv.length == 2)
                            s.attendance.put(kv[0], kv[1]);
                    }
                }
                if (cols.length > 7 && !unquote(cols[7]).isEmpty()) {
                    String mk = unquote(cols[7]);
                    String[] pairs = mk.split("\\^");
                    for (String p : pairs) {
                        if (p.isEmpty())
                            continue;
                        String[] kv = p.split(":", 2);
                        if (kv.length == 2) {
                            try {
                                s.marks.put(kv[0], Integer.parseInt(kv[1]));
                            } catch (Exception ex) {
                            }
                        }
                    }
                }

                // NEW: load feeHistory (col index 8)
                if (cols.length > 8 && !unquote(cols[8]).isEmpty()) {
                    String fh = unquote(cols[8]);
                    // fh = rec1 ^ rec2 ^ rec3 ...
                    String[] recs = fh.split("\\^");
                    for (String r : recs) {
                        if (r.isEmpty())
                            continue;
                        // record: date ~ amount ~ classFeeAtTime ~ note
                        String[] parts = r.split("~", 4);
                        if (parts.length >= 3) {
                            try {
                                String date = parts[0];
                                double amount = Double.parseDouble(parts[1]);
                                double classFee = Double.parseDouble(parts[2]);
                                String note = parts.length >= 4 ? parts[3] : "";
                                Student.FeeRecord fr = new Student.FeeRecord(amount, classFee, note);
                                // override generated date with saved date:
                                fr.date = date;
                                s.feeHistory.add(fr);
                            } catch (Exception ex) {
                                // skip malformed record
                            }
                        }
                    }
                }

                students.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadTeachers() {
        File f = new File(TEACHERS_FILE);
        if (!f.exists())
            return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] cols = splitCsv(line, 5);
                if (cols.length < 4)
                    continue;
                String id = unquote(cols[0]);
                String name = unquote(cols[1]);
                String contact = unquote(cols[2]);
                String password = unquote(cols[3]);
                Teacher t = new Teacher(id, name, contact, password);
                if (cols.length > 4 && !unquote(cols[4]).isEmpty()) {
                    String encoded = unquote(cols[4]);
                    String[] classParts = encoded.split(";");
                    for (String cp : classParts) {
                        if (cp.trim().isEmpty())
                            continue;
                        String[] kv = cp.split("~", 2);
                        if (kv.length == 2) {
                            String cname = kv[0];
                            String[] subs = kv[1].split("\\|");
                            List<String> list = new ArrayList<>();
                            for (String s : subs)
                                if (!s.trim().isEmpty())
                                    list.add(s.trim());
                            t.assignedClassSubjects.put(cname, list);
                        }
                    }
                }
                teachers.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveData() {
        saveClasses();
        saveStudents();
        saveTeachers();
        saveAdminPassword();

    }

    private static void saveClasses() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CLASSES_FILE))) {
            for (ClassRoom c : classes) {
                String subj = String.join("|", c.subjects);
                pw.println(escapeCsv(c.name) + "," + escapeCsv(subj) + "," + escapeCsv(String.valueOf(c.fee)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveStudents() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(STUDENTS_FILE))) {
            for (Student s : students) {
                StringBuilder att = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> e : s.attendance.entrySet()) {
                    if (!first)
                        att.append("^");
                    first = false;
                    att.append(e.getKey()).append("~").append(e.getValue());
                }
                StringBuilder mk = new StringBuilder();
                first = true;
                for (Map.Entry<String, Integer> e : s.marks.entrySet()) {
                    if (!first)
                        mk.append("^");
                    first = false;
                    mk.append(e.getKey()).append(":").append(e.getValue());
                }

                // NEW: serialize feeHistory
                StringBuilder fh = new StringBuilder();
                first = true;
                for (Student.FeeRecord fr : s.feeHistory) {
                    if (!first)
                        fh.append("^");
                    first = false;
                    // record fields: date ~ amount ~ classFeeAtTime ~ note
                    String dateEsc = fr.date.replace("~", " ").replace("^", " "); // basic sanitizing of separators
                    String noteEsc = fr.note == null ? "" : fr.note.replace("~", " ").replace("^", " ");
                    fh.append(dateEsc).append("~").append(fr.amount).append("~").append(fr.classFeeAtTime).append("~")
                            .append(noteEsc);
                }

                pw.println(
                        escapeCsv(s.id) + "," +
                                escapeCsv(s.name) + "," +
                                escapeCsv(s.contact) + "," +
                                escapeCsv(s.klass) + "," +
                                escapeCsv(s.password) + "," +
                                escapeCsv(String.valueOf(s.feePaid)) + "," +
                                escapeCsv(att.toString()) + "," +
                                escapeCsv(mk.toString()) + "," +
                                escapeCsv(fh.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveTeachers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TEACHERS_FILE))) {
            for (Teacher t : teachers) {
                StringBuilder enc = new StringBuilder();
                boolean firstClass = true;
                for (Map.Entry<String, List<String>> en : t.assignedClassSubjects.entrySet()) {
                    if (!firstClass)
                        enc.append(";");
                    firstClass = false;
                    enc.append(en.getKey()).append("~").append(String.join("|", en.getValue()));
                }
                pw.println(escapeCsv(t.id) + "," + escapeCsv(t.name) + "," + escapeCsv(t.contact) + ","
                        + escapeCsv(t.password) + "," + escapeCsv(enc.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Utilities & Getters =====
    public static ClassRoom getClassByName(String name) {
        if (name == null)
            return null;
        for (ClassRoom c : classes)
            if (name.equals(c.name))
                return c;
        return null;
    }

    public static Student getStudentById(String id) {
        if (id == null)
            return null;
        for (Student s : students)
            if (id.equals(s.id))
                return s;
        return null;
    }

    public static Teacher getTeacherById(String id) {
        if (id == null)
            return null;
        for (Teacher t : teachers)
            if (id.equals(t.id))
                return t;
        return null;
    }

    public static boolean studentIdExists(String id) {
        return getStudentById(id) != null;
    }

    public static boolean teacherIdExists(String id) {
        return getTeacherById(id) != null;
    }

    public static String todayDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    // Is a particular class-subject already assigned to any teacher?
    public static boolean isSubjectAssignedToAnyTeacher(String className, String subject) {
        for (Teacher t : teachers) {
            List<String> list = t.assignedClassSubjects.get(className);
            if (list != null && list.contains(subject))
                return true;
        }
        return false;
    }

    // Assign subject to teacher (ensures exclusivity)
    public static boolean assignSubjectsToTeacher(String teacherId, String className, List<String> subjectsOut) {
        Teacher t = getTeacherById(teacherId);
        if (t == null)
            return false;
        // ensure not assigned elsewhere
        for (String s : subjectsOut) {
            if (isSubjectAssignedToAnyTeacher(className, s)) {
                // if already assigned to same teacher it's ok
                List<String> cur = t.assignedClassSubjects.get(className);
                if (cur == null || !cur.contains(s))
                    return false;
            }
        }
        t.assignedClassSubjects.putIfAbsent(className, new ArrayList<>());
        for (String s : subjectsOut) {
            List<String> cur = t.assignedClassSubjects.get(className);
            if (!cur.contains(s))
                cur.add(s);
        }
        saveData();
        return true;
    }

    // Update teacher's assigned subjects for a class (replace list)
    public static boolean updateTeacherAssignedSubjects(String teacherId, String className, List<String> newSubjects) {
        Teacher t = getTeacherById(teacherId);
        if (t == null)
            return false;
        // check exclusivity: newSubjects cannot be assigned to other teachers
        for (Teacher other : teachers) {
            if (other.id.equals(teacherId))
                continue;
            List<String> list = other.assignedClassSubjects.get(className);
            if (list != null) {
                for (String s : newSubjects)
                    if (list.contains(s))
                        return false;
            }
        }
        if (newSubjects.isEmpty())
            t.assignedClassSubjects.remove(className);
        else
            t.assignedClassSubjects.put(className, new ArrayList<>(newSubjects));
        saveData();
        return true;
    }

    // ===== CRUD operations with cascades =====
    // Add / Update Student
    public static boolean addStudent(Student s) {
        if (s == null)
            return false;
        if (studentIdExists(s.id))
            return false;
        students.add(s);
        ClassRoom c = getClassByName(s.klass);
        if (c != null && !c.studentIds.contains(s.id))
            c.studentIds.add(s.id);
        saveData();
        return true;
    }

    // Add student and return assigned ID
    public static String addStudentAuto(String name, String contact, String klass, String password) {
        String id = generateUniqueStudentId();
        Student s = new Student(id, name, contact, klass, password);
        addStudent(s); // reuse existing addStudent(Student s)
        return id;
    }

    // Add teacher and return assigned ID
    public static String addTeacherAuto(String name, String contact, String password) {
        String id = generateUniqueTeacherId();
        Teacher t = new Teacher(id, name, contact, password);
        addTeacher(t); // reuse existing addTeacher(Teacher t)
        return id;
    }

    public static boolean updateStudent(String oldId, Student newS) {
        Student existing = getStudentById(oldId);
        if (existing == null)
            return false;
        // If changing ID, ensure integer and uniqueness
        if (!oldId.equals(newS.id)) {
            if (studentIdExists(newS.id))
                return false;
        }
        // If class changed, move id
        if (!existing.klass.equals(newS.klass)) {
            ClassRoom from = getClassByName(existing.klass);
            ClassRoom to = getClassByName(newS.klass);
            if (from != null)
                from.studentIds.remove(existing.id);
            if (to != null && !to.studentIds.contains(newS.id))
                to.studentIds.add(newS.id);
        } else {
            // if id changed but class same, update list element
            ClassRoom cr = getClassByName(existing.klass);
            if (cr != null) {
                cr.studentIds.remove(existing.id);
                if (!cr.studentIds.contains(newS.id))
                    cr.studentIds.add(newS.id);
            }
        }
        // replace fields
        existing.id = newS.id;
        existing.name = newS.name;
        existing.contact = newS.contact;
        existing.klass = newS.klass;
        existing.password = newS.password;
        existing.feePaid = newS.feePaid;
        existing.attendance = new LinkedHashMap<>(newS.attendance);
        existing.marks = new LinkedHashMap<>(newS.marks);
        saveData();
        return true;
    }

    // Delete student and remove from class references
    public static boolean deleteStudent(String id) {
        Student s = getStudentById(id);
        if (s == null)
            return false;
        // remove from class student list
        ClassRoom cr = getClassByName(s.klass);
        if (cr != null)
            cr.studentIds.remove(id);
        // remove from students list
        Iterator<Student> it = students.iterator();
        while (it.hasNext()) {
            Student ss = it.next();
            if (ss.id.equals(id)) {
                it.remove();
                break;
            }
        }
        saveData();
        return true;
    }

    // Add / Update Teacher
    public static boolean addTeacher(Teacher t) {
        if (t == null)
            return false;
        if (teacherIdExists(t.id))
            return false;
        teachers.add(t);
        saveData();
        return true;
    }

    public static boolean updateTeacher(String oldId, Teacher newT) {
        Teacher existing = getTeacherById(oldId);
        if (existing == null)
            return false;
        if (!oldId.equals(newT.id)) {
            if (teacherIdExists(newT.id))
                return false;
        }
        existing.id = newT.id;
        existing.name = newT.name;
        existing.contact = newT.contact;
        existing.password = newT.password;
        existing.assignedClassSubjects = new LinkedHashMap<>(newT.assignedClassSubjects);
        saveData();
        return true;
    }

    // Delete teacher and remove assignments
    public static boolean deleteTeacher(String id) {
        Teacher t = getTeacherById(id);
        if (t == null)
            return false;
        // remove assigned references only exist in teacher object; no cross links
        // needed
        Iterator<Teacher> it = teachers.iterator();
        while (it.hasNext()) {
            Teacher tt = it.next();
            if (tt.id.equals(id)) {
                it.remove();
                break;
            }
        }
        saveData();
        return true;
    }

    // Subject management for a class (add/update/delete)
    public static boolean addSubjectToClass(String className, String subject) {
        ClassRoom c = getClassByName(className);
        if (c == null)
            return false;
        if (subject == null || subject.trim().isEmpty())
            return false;
        subject = subject.trim();
        if (c.subjects.contains(subject))
            return false;
        c.subjects.add(subject);
        saveData();
        return true;
    }

    public static boolean updateSubjectInClass(String className, String oldSubject, String newSubject) {
        ClassRoom c = getClassByName(className);
        if (c == null)
            return false;
        if (!c.subjects.contains(oldSubject))
            return false;
        newSubject = newSubject.trim();
        if (newSubject.isEmpty())
            return false;
        // do not allow duplicate
        if (c.subjects.contains(newSubject) && !oldSubject.equals(newSubject))
            return false;
        // update class subject list
        int idx = c.subjects.indexOf(oldSubject);
        c.subjects.set(idx, newSubject);
        // update students marks key if exists
        for (Student s : students) {
            if (s.marks.containsKey(oldSubject)) {
                Integer val = s.marks.remove(oldSubject);
                s.marks.put(newSubject, val);
            }
        }
        // update teacher assignments
        for (Teacher t : teachers) {
            for (Map.Entry<String, List<String>> en : t.assignedClassSubjects.entrySet()) {
                if (en.getKey().equals(className)) {
                    List<String> list = en.getValue();
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).equals(oldSubject))
                            list.set(i, newSubject);
                    }
                }
            }
        }
        saveData();
        return true;
    }

    public static boolean deleteSubjectFromClass(String className, String subject) {
        ClassRoom c = getClassByName(className);
        if (c == null)
            return false;
        if (!c.subjects.contains(subject))
            return false;
        c.subjects.remove(subject);
        // remove marks from students
        for (Student s : students)
            s.marks.remove(subject);
        // remove assignments from teachers
        for (Teacher t : teachers) {
            List<String> list = t.assignedClassSubjects.get(className);
            if (list != null) {
                list.remove(subject);
                if (list.isEmpty())
                    t.assignedClassSubjects.remove(className);
            }
        }
        saveData();
        return true;
    }

    // Update class fee
    public static boolean setClassFee(String className, double fee) {
        ClassRoom c = getClassByName(className);
        if (c == null)
            return false;
        if (fee < 0)
            return false;
        c.fee = fee;
        saveData();
        return true;
    }

    // Pay fee by student id (adds to feePaid)
    public static boolean payFeeForStudent(String studentId, double amount) {
        Student s = getStudentById(studentId);
        if (s == null)
            return false;

        ClassRoom c = getClassByName(s.klass);
        if (c == null)
            return false;

        // Prevent overpay
        double pending = c.fee - s.feePaid;
        if (amount > pending)
            return false;

        s.feePaid += amount;

        // Inside DataStore.java (or wherever you add fee record)
        DataStore.Student.FeeRecord fr = new DataStore.Student.FeeRecord(amount, c.fee, "Payment");
        s.feeHistory.add(fr);

        saveData();
        return true;
    }

    // Mark attendance for a student for today (P/A)
    public static boolean markAttendanceForStudent(String studentId, String date, String value) {
        Student s = getStudentById(studentId);
        if (s == null)
            return false;
        if (!"P".equals(value) && !"A".equals(value))
            return false;
        s.attendance.put(date, value);
        saveData();
        return true;
    }

    // Enter marks for student subject (0-100)
    public static boolean setMarkForStudent(String studentId, String subject, int mark) {
        Student s = getStudentById(studentId);
        if (s == null)
            return false;
        if (mark < 0 || mark > 100)
            return false;
        s.marks.put(subject, mark);
        saveData();
        return true;
    }

    // Helper: remove all previous data (clear files) - per your request "remove
    // previous data"
    // NOTE: Use with caution. We'll provide method but not call it automatically.
    public static void clearAllDataFiles() {
        try {
            new File(CLASSES_FILE).delete();
            new File(STUDENTS_FILE).delete();
            new File(TEACHERS_FILE).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean updateTeacherAssignedSubjectsAdvanced(String teacherId, String oldSubject, String newClass,
            String newSubject) {
        Teacher t = getTeacherById(teacherId);
        if (t == null)
            return false;

        // Remove old subject from any class it was assigned in this teacher
        String oldClassFound = null;
        for (Map.Entry<String, List<String>> en : t.assignedClassSubjects.entrySet()) {
            if (en.getValue().contains(oldSubject)) {
                oldClassFound = en.getKey();
                en.getValue().remove(oldSubject);
                if (en.getValue().isEmpty())
                    t.assignedClassSubjects.remove(en.getKey());
                break;
            }
        }

        // Check exclusivity: new class-subject not assigned to others
        if (isSubjectAssignedToAnyTeacher(newClass, newSubject))
            return false;

        // Check teacher max 3 subjects
        int currentTotal = t.totalAssignedSubjects();
        if (currentTotal >= 3)
            return false;

        // Assign new subject
        t.assignedClassSubjects.putIfAbsent(newClass, new ArrayList<>());
        t.assignedClassSubjects.get(newClass).add(newSubject);

        saveData();
        return true;
    }
    // ---------- DataStore.java additions ----------

    // ===== Generate unique Student ID (1-1000) =====
    public static String generateUniqueStudentId() {
        for (int i = 1; i <= 1000; i++) {
            String id = String.valueOf(i);
            if (!studentIdExists(id))
                return id;
        }
        throw new RuntimeException("No available Student IDs");
    }

    // ===== Generate unique Teacher ID (1-1000) =====
    public static String generateUniqueTeacherId() {
        for (int i = 1; i <= 1000; i++) {
            String id = String.valueOf(i);
            if (!teacherIdExists(id))
                return id;
        }
        throw new RuntimeException("No available Teacher IDs");
    }

    // ===== Check if student ID exists =====

}
