package com.vncodelab.service.ischolar;

import com.vncodelab.entity.ischolar.Item;
import com.vncodelab.entity.ischolar.Option;
import com.vncodelab.json.ischolar.Jsmind;
import com.vncodelab.respository.PhraseRepository;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GenDocService {

    @Autowired
    PhraseRepository phraseRepository;

    public ResponseEntity<InputStreamResource> genDoc(Jsmind jsmind) {
        try {
            String template = "/Docx/Paper.docx";
            if (jsmind.docType == 1) {
                template = "/Docx/Report.docx";
            } else if (jsmind.docType == 2) {
                template = "/Docx/Đề án.docx";
            }

            XWPFDocument doc = new XWPFDocument(GenDocService.class.getResourceAsStream(template)); //Appengine
            //   XWPFDocument doc = new XWPFDocument(new FileInputStream(new File(getClass().getClassLoader().getResource("Paper.docx").getFile()))); //Appengine
            Map<String, String> phList = new HashMap<>();
            //  Research research = new Research();

            phList.put("organization", jsmind.meta.author_affiliation);
            phList.put("type", jsmind.docType + "");
            phList.put("title", jsmind.meta.title);
            phList.put("student", jsmind.meta.student_name);
            phList.put("teacher", jsmind.meta.teacher_name);

            phList.put("author", jsmind.meta.author_name);
            phList.put("email", jsmind.meta.author_email);
            phList.put("affiliation", jsmind.meta.author_affiliation);

            phantach(phList, "literature", jsmind.data.getChild("Literature"));
            phantach(phList, "method", jsmind.data.getChild("Methodology"));
            phantach(phList, "finding", jsmind.data.getChild("Results and Discussion"));

            //  InputStream is = SectionRepository.class.getResourceAsStream("/A_phrase.json"); //Appengine1

            ArrayList<Item> listPhrases = phraseRepository.getAllPhrases();

            //   AppData appData = new Gson().fromJson(new InputStreamReader(new ClassPathResource("Sections_phrase.json").getInputStream(), "UTF-8"), AppData.class);
            for (XWPFParagraph p : doc.getParagraphs()) {
                buildPH(p, phList, listPhrases, jsmind);
            }

//            Locale.setDefault(new Locale("vi", "VN"));
//            ResourceBundle mybundle = ResourceBundle.getBundle("text", new UTF8Control());
//            Enumeration<String> keys = mybundle.getKeys();
//            while (keys.hasMoreElements()) {
//                String key = keys.nextElement();
//                String value = mybundle.getString(key);
//                if (jsmind.meta.language == "English")
//                    phList.put(key, value);
//                else
//                    phList.put(key, key.replace("_", " "));
//            }

            String resut = "";
            for (XWPFParagraph p : doc.getParagraphs()) {
                resut = resut + replace2(p, phList);
            }

            InRaManHinh(resut);
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            doc.write(b); // doc should be a XWPFDocument
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(b.toByteArray()));
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            String fileName = jsmind.meta.title.trim() + ".docx";
            headers.add("Content-Disposition", "attachment; filename=" + fileName);

            doc.close();
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(b.size())
                    .contentType(MediaType.parseMediaType("application/octet-stream;charset=UTF-8"))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    private void phantach(Map<String, String> phList, String authorKey, String authorValues) {
        String arr[] = authorValues.split("\\|");
        for (int i = 0; i < arr.length; i++) {
            phList.put(authorKey + (i + 1), arr[i].trim());
        }

    }

    private String lookUp(ArrayList<Item> listPhrases, String key, Jsmind jsmind) {
        String s = "";
        ArrayList<Item> list = new ArrayList<>();
        for (Item item : listPhrases) {
            for (Option option : item.getPhrases()) {
                if (option.getSection().equals(key)) {
                    list.add(item);
                    break;
                } else
                    break;
            }
        }

        for (Item item : list) {
            s = s + " (" + item.getId().trim() + ") ";
            for (int i = 1; i <= jsmind.meta.number_of_sample; i++)
                if (list.size() > 0) {
                    int r = new Random().nextInt(item.getPhrases().size());
                    if (jsmind.meta.language.equals("English")) {
                        s = s + item.getPhrases().get(r).getOption().trim() + " "; //Vietnam
                    } else
                        s = s + item.getPhrases().get(r).getDescription().trim() + " "; //English
                }
            s = s + "\n";
        }
        return s;
    }

    public void buildPH(XWPFParagraph p, Map<String, String> data, ArrayList<Item> listPhrases, Jsmind jsmind) {
        String pText = p.getText(); // complete paragraph as string
        if (pText.contains("${")) { // if paragraph does not include our pattern, ignore
            TreeMap<Integer, XWPFRun> posRuns = getPosToRuns(p);
            Pattern pat = Pattern.compile("\\$\\{(.+?)\\}");
            Matcher m = pat.matcher(pText);
            while (m.find()) { // for all patterns in the paragraph
                String g = m.group(1);  // extract key start and end pos
                if (data.get(g) == null) {
                    String s = lookUp(listPhrases, g, jsmind);
                    if (s != null)
                        data.put(g, s);
                }
            }
        }
    }

    public String replace2(XWPFParagraph p, Map<String, String> data) {
        String result = "";
        String pText = p.getText(); // complete paragraph as string
        if (pText.contains("${")) { // if paragraph does not include our pattern, ignore
            TreeMap<Integer, XWPFRun> posRuns = getPosToRuns(p);
            Pattern pat = Pattern.compile("\\$\\{(.+?)\\}");
            Matcher m = pat.matcher(pText);
            while (m.find()) { // for all patterns in the paragraph
                String g = m.group(1);  // extract key start and end pos
                int s = m.start(1);
                int e = m.end(1);
                String key = g;
                String x = data.get(key);
                if (x == null)
                    x = "";
                SortedMap<Integer, XWPFRun> range = posRuns.subMap(s - 2, true, e + 1, true); // get runs which contain the pattern
                boolean found1 = false; // found $
                boolean found2 = false; // found {
                boolean found3 = false; // found }
                XWPFRun prevRun = null; // previous run handled in the loop
                XWPFRun found2Run = null; // run in which { was found
                int found2Pos = -1; // pos of { within above run
                for (XWPFRun r : range.values()) {
                    if (r == prevRun) {
                        continue; // this run has already been handled
                    }

                    if (found3)
                        break; // done working on current key pattern
                    prevRun = r;
                    for (int k = 0; ; k++) { // iterate over texts of run r
                        if (found3)
                            break;
                        String txt = null;
                        try {
                            txt = r.getText(k); // note: should return null, but throws exception if the text does not exist
                        } catch (Exception ex) {

                        }
                        if (txt == null)
                            break; // no more texts in the run, exit loop
                        if (txt.contains("$") && !found1) {  // found $, replace it with value from data map
                            txt = txt.replaceFirst("\\$", getPart(x, 0));
                            found1 = true;
                        }
                        if (txt.contains("{") && !found2 && found1) {
                            found2Run = r; // found { replace it with empty string and remember location
                            found2Pos = txt.indexOf('{');

                            txt = txt.replace("\\{", getPart(x, 1));
                            found2 = true;
                        }
                        if (found1 && found2 && !found3) { // find } and set all chars between { and } to blank
                            if (txt.contains("}")) {
                                if (r == found2Run) { // complete pattern was within a single run
                                    txt = txt.substring(0, found2Pos) + txt.substring(txt.indexOf('}'));
                                } else // pattern spread across multiple runs
                                    txt = txt.substring(txt.indexOf('}'));
                            } else if (r == found2Run) // same run as { but no }, remove all text starting at {
                                txt = txt.substring(0, found2Pos);
                            else
                                txt = ""; // run between { and }, set text to blank
                        }
                        if (txt.contains("}") && !found3) {  //Chay lan 2
                            txt = txt.replaceFirst("\\}", "");
                            found3 = true;
                        }
                        if (!getPart(x, 1).isEmpty()) {  //Co part 2
                            r.isHighlighted(true);
                        }
                        r.setText(txt, k);
                    }
                }
                if (!getPart(x, 1).isEmpty()) {
                    XWPFRun run = p.createRun();
                    String part2 = ": " + getPart(x, 1);
                    if (part2.equals(": null")) {
                        XWPFRun run1 = p.createRun();
                    }
                    run.setText(part2);
                }
            }
            result = result + p.getText() + "\n";
        }
        return result;
    }


    private void InRaManHinh(String string) {
        InRaManHinh(string, false);
    }

    String strMessage = "";

    private void InRaManHinh(String string, boolean err) {
        strMessage += string + "\n";
        if (err)
            System.err.println(string);
        else
            System.out.println(string);
    }

    private static String getPart(String x, int i) {
        String arr[] = x.split("\\|");
        if (arr.length > i)
            return arr[i];
        else
            return "";
    }

    public static TreeMap<Integer, XWPFRun> getPosToRuns(XWPFParagraph paragraph) {
        int pos = 0;
        TreeMap<Integer, XWPFRun> map = new TreeMap<Integer, XWPFRun>();
        for (XWPFRun run : paragraph.getRuns()) {
            String runText = run.text();
            if (runText != null && runText.length() > 0) {
                for (int i = 0; i < runText.length(); i++) {
                    map.put(pos + i, run);
                }
                pos += runText.length();
            }
        }
        map.put(pos + 1, paragraph.createRun());
        return map;
    }

    public class UTF8Control extends ResourceBundle.Control {
        public ResourceBundle newBundle
                (String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            // The below is a copy of the default implementation.
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    // Only this line is changed to make it to read properties files as UTF-8.
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }
}