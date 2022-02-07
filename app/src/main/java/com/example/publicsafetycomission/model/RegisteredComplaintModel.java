package com.example.publicsafetycomission.model;

public class RegisteredComplaintModel {
    String complaint_source;
    String complaint_council;
    String complaint_detail;
    String complaint_entry_timestamp;
    String complainant_name;
    String district_name;
    String complaint_status_title;
    String complaint_status_color;
    String complaint_category_name;

    public RegisteredComplaintModel() {
    }

    public RegisteredComplaintModel(String complaint_source, String complaint_council, String complaint_detail, String complaint_entry_timestamp, String complainant_name, String district_name, String complaint_status_title, String complaint_status_color, String complaint_category_name) {
        this.complaint_source = complaint_source;
        this.complaint_council = complaint_council;
        this.complaint_detail = complaint_detail;
        this.complaint_entry_timestamp = complaint_entry_timestamp;
        this.complainant_name = complainant_name;
        this.district_name = district_name;
        this.complaint_status_title = complaint_status_title;
        this.complaint_status_color = complaint_status_color;
        this.complaint_category_name = complaint_category_name;
    }

    public String getComplaint_source() {
        return complaint_source;
    }

    public void setComplaint_source(String complaint_source) {
        this.complaint_source = complaint_source;
    }

    public String getComplaint_council() {
        return complaint_council;
    }

    public void setComplaint_council(String complaint_council) {
        this.complaint_council = complaint_council;
    }

    public String getComplaint_detail() {
        return complaint_detail;
    }

    public void setComplaint_detail(String complaint_detail) {
        this.complaint_detail = complaint_detail;
    }

    public String getComplaint_entry_timestamp() {
        return complaint_entry_timestamp;
    }

    public void setComplaint_entry_timestamp(String complaint_entry_timestamp) {
        this.complaint_entry_timestamp = complaint_entry_timestamp;
    }

    public String getComplainant_name() {
        return complainant_name;
    }

    public void setComplainant_name(String complainant_name) {
        this.complainant_name = complainant_name;
    }

    public String getDistrict_name() {
        return district_name;
    }

    public void setDistrict_name(String district_name) {
        this.district_name = district_name;
    }

    public String getComplaint_status_title() {
        return complaint_status_title;
    }

    public void setComplaint_status_title(String complaint_status_title) {
        this.complaint_status_title = complaint_status_title;
    }

    public String getComplaint_status_color() {
        return complaint_status_color;
    }

    public void setComplaint_status_color(String complaint_status_color) {
        this.complaint_status_color = complaint_status_color;
    }

    public String getComplaint_category_name() {
        return complaint_category_name;
    }

    public void setComplaint_category_name(String complaint_category_name) {
        this.complaint_category_name = complaint_category_name;
    }
}
