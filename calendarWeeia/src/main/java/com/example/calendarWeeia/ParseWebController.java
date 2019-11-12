package com.example.calendarWeeia;


import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
@RestController
public class ParseWebController {

    @RequestMapping(value = "/calendar/month/{numberMonth}" )
    @ResponseBody
    public ResponseEntity<Resource> getCalendar(@PathVariable int numberMonth) throws IOException {

        Elements elements,eventNamesElements;
        Document doc = Jsoup.connect("http://www.weeia.p.lodz.pl/pliki_strony_kontroler/kalendarz.php?rok=2019&miesiac="+numberMonth+"&lang=1").get();
        elements = doc.select("a.active");
        eventNamesElements = doc.select("div.InnerBox");
        int year=2019;
        List<String> days = new ArrayList<>();
        List<String> eventNames = new ArrayList<>();

        elements.stream().forEach(dayElement -> days.add(dayElement.text()));

        eventNamesElements.stream().forEach(eventNameElement -> eventNames.add(eventNameElement.text()));

        ICalendar iCalendar = new ICalendar();

        for (int index = 0; index < days.size(); index++) {
            VEvent vEvent = new VEvent();
            vEvent.setSummary(eventNames.get(index));
            Calendar date = Calendar.getInstance();
            date.set(year, numberMonth - 1, Integer.parseInt(days.get(index)));
            vEvent.setDateStart(date.getTime());
            vEvent.setDateEnd(date.getTime());
            iCalendar.addEvent(vEvent);
        }

        File calendarFile = new File( numberMonth + ".ics");
        Biweekly.write(iCalendar).go(calendarFile);

        Resource fileStreamResource = new FileSystemResource(calendarFile);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(fileStreamResource);
    }
}

