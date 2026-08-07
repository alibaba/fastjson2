package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.Era;
import java.time.chrono.MinguoChronology;
import java.time.chrono.MinguoDate;

final class ObjectReaderImplMinguoDate
        extends ObjectReaderPrimitive<MinguoDate> {
    static final ObjectReaderImplMinguoDate INSTANCE = new ObjectReaderImplMinguoDate();

    ObjectReaderImplMinguoDate() {
        super(MinguoDate.class);
    }

    @Override
    public MinguoDate readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        String str = jsonReader.readString();
        if (str == null || str.isEmpty()) {
            return null;
        }
        return (MinguoDate) parseChronoLocalDate(str, MinguoChronology.INSTANCE);
    }

    @Override
    public MinguoDate readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        String str = jsonReader.readString();
        if (str == null) {
            return null;
        }
        return (MinguoDate) parseChronoLocalDate(str, MinguoChronology.INSTANCE);
    }

    private static ChronoLocalDate parseChronoLocalDate(String str, Chronology chronology) {
        int firstSpace = str.indexOf(' ');
        int secondSpace = firstSpace < 0 ? -1 : str.indexOf(' ', firstSpace + 1);
        if (firstSpace > 0 && secondSpace > firstSpace + 1) {
            String chronologyId = str.substring(0, firstSpace);
            if (!chronology.getId().equals(chronologyId)) {
                throw new DateTimeException("Invalid chronology: " + chronologyId);
            }

            String eraText = str.substring(firstSpace + 1, secondSpace);
            Era era = findEra(chronology, eraText);
            if (era == null) {
                throw new DateTimeException("Invalid era: " + eraText);
            }

            int[] ymd = parseChronoDatePart(str.substring(secondSpace + 1));
            return chronology.date(era, ymd[0], ymd[1], ymd[2]);
        }

        LocalDate isoDate = LocalDate.parse(str);
        return chronology.date(isoDate);
    }

    private static Era findEra(Chronology chronology, String eraText) {
        for (Era era : chronology.eras()) {
            if (eraText.equals(era.toString())) {
                return era;
            }
            if (era instanceof Enum && eraText.equals(((Enum) era).name())) {
                return era;
            }
        }
        return null;
    }

    private static int[] parseChronoDatePart(String text) {
        int firstDash = text.indexOf('-');
        int secondDash = firstDash < 0 ? -1 : text.indexOf('-', firstDash + 1);
        if (firstDash <= 0 || secondDash <= firstDash + 1 || secondDash + 1 >= text.length()) {
            throw new DateTimeException("Invalid date: " + text);
        }

        int year = parseChronoInt(text, 0, firstDash);
        int month = parseChronoInt(text, firstDash + 1, secondDash);
        int day = parseChronoInt(text, secondDash + 1, text.length());
        return new int[]{year, month, day};
    }

    private static int parseChronoInt(String text, int start, int end) {
        try {
            return Integer.parseInt(text.substring(start, end));
        } catch (NumberFormatException ex) {
            throw new DateTimeException("Invalid number: " + text.substring(start, end), ex);
        }
    }
}
