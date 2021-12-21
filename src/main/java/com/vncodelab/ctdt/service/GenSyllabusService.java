package com.vncodelab.ctdt.service;

import com.vncodelab.jsontoexcel.ExcelReader;
import com.vncodelab.model.ctdt.HocPhan;
import org.apache.poi.xwpf.usermodel.*;
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
public class GenSyllabusService {
    public static void main(String[] args) {
        ExcelReader excelReader = new ExcelReader<HocPhan>();
        try {
            ArrayList<HashMap<String, Object>> list = excelReader.readExcel("/Users/xuanlam/OneDrive/OneDrive - National Economics University/0. NEU/9. Cong viec/41. Cập nhật đề cương/PLO/Thông tin.xlsx", 0);
            GenSyllabusService genDocService = new GenSyllabusService();
            for (HashMap<String, Object> item : list) {
                genDocService.genDoc(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ResponseEntity<InputStreamResource> genDoc(HashMap<String, Object> phList) {
        try {
            String template = "/ctdt/Mẫu đề cương KHMT.docx";
            XWPFDocument doc = new XWPFDocument(GenSyllabusService.class.getResourceAsStream(template)); //Appengine
            //   XWPFDocument doc = new XWPFDocument(new FileInputStream(new File(getClass().getClassLoader().getResource("Paper.docx").getFile()))); //Appengine
            String resut = "";
            for (IBodyElement p : doc.getBodyElements()) {
                resut = resut + replace2(p, phList);
            }
            if(phList.get("khoikt_KHMT")!=null) {

                String fileName = phList.get("code") + "_" + phList.get("nameVi") + "_" + phList.get("soTC") + "TC_Chi tiết.docx";
                fileName = VNCharacterUtils.removeAccent(fileName);

                InRaManHinh(resut);
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                FileOutputStream out = new FileOutputStream("/Users/xuanlam/OneDrive/OneDrive - National Economics University/0. NEU/1. Chuong trinh dao tao & de cuong/3. De Cuong K63/" + fileName);
                doc.write(out);
                doc.write(b); // doc should be a XWPFDocument


                InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(b.toByteArray()));
                HttpHeaders headers = new HttpHeaders();
                headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                headers.add("Pragma", "no-cache");
                headers.add("Expires", "0");

                headers.add("Content-Disposition", "attachment; filename=" + fileName);
                doc.close();
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentLength(b.size())
                        .contentType(MediaType.parseMediaType("application/octet-stream;charset=UTF-8"))
                        .body(resource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String replace2(IBodyElement p, Map<String, Object> data) {
        String result = "";
        String pText = "";
        if (p instanceof XWPFParagraph)
            pText = ((XWPFParagraph) p).getText(); // complete paragraph as string
        else if (p instanceof XWPFTable)
            pText = ((XWPFTable) p).getText();
        else
            return null;
        if (pText.contains("${")) { // if paragraph does not include our pattern, ignore
            TreeMap<Integer, XWPFRun> posRuns;
            if (p instanceof XWPFParagraph) {
                posRuns = getPosToRuns(((XWPFParagraph) p));
                Pattern pat = Pattern.compile("\\$\\{(.+?)\\}");
                Matcher m = pat.matcher(pText);
                while (m.find()) { // for all patterns in the paragraph
                    String g = m.group(1);  // extract key start and end pos
                    int s = m.start(1);
                    int e = m.end(1);
                    String key = g;
                    String x = "" + data.get(key);
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
                            r.setText(txt, k);
                        }
                    }
//                if (!getPart(x, 1).isEmpty()) {
//                    XWPFRun run = ((XWPFParagraph) p).createRun();
//                    String part2 = ": " + getPart(x, 1);
//                    if (part2.equals(": null")) {
//                        XWPFRun run1 = ((XWPFParagraph) p).createRun();
//                    }
//                    run.setText(part2);
//                }
                }
                result = result + ((XWPFParagraph) p).getText() + "\n";
            }
            if (p instanceof XWPFTable) {
                for (XWPFTableRow row : ((XWPFTable) p).getRows()) {

                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (IBodyElement pc : cell.getBodyElements()) {
                            replace2(pc, data);
                        }
                    }
                }
            }
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

//    public static TreeMap<Integer, XWPFRun> getPosToRuns(XWPFTable paragraph) {
//        int pos = 0;
//        TreeMap<Integer, XWPFRun> map = new TreeMap<Integer, XWPFRun>();
//        for (XWPFRun run : paragraph.getRuns()) {
//            String runText = run.text();
//            if (runText != null && runText.length() > 0) {
//                for (int i = 0; i < runText.length(); i++) {
//                    map.put(pos + i, run);
//                }
//                pos += runText.length();
//            }
//        }
//        map.put(pos + 1, paragraph.createRun());
//        return map;
//    }

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
