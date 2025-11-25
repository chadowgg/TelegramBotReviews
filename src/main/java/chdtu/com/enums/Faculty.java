package chdtu.com.enums;

public enum Faculty {
    FGT("ФТБРП", "Факультет технологій, будівництва та раціонального природокористування"),
    FETAM("ФЕТАМ", "Факультет електронних технологій, автотранспорту та машинобудування"),
    FEU("ФЕУ", "Факультет економіки та управління"),
    FITS("ФІТС", "Факультет інформаційних технологій і систем"),
    FGTU("ФГТ", "Факультет гуманітарних технологій"),
    FIS("ФІС", "Навчально-науковий центр по роботі з іноземними студентами");

    private final String code;
    private final String displayName;

    Faculty(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

}
