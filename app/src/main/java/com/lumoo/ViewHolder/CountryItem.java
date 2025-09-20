package com.lumoo.ViewHolder;

public class CountryItem {
    private String countryName;
    private String countryCode; // ISO kod, drawable ile eşleşecek

    public CountryItem(String countryName, String countryCode) {
        this.countryName = countryName;
        this.countryCode = countryCode.toLowerCase();
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
