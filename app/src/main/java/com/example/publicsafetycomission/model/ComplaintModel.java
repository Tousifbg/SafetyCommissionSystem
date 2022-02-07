package com.example.publicsafetycomission.model;

import java.io.File;
import java.util.List;

public class ComplaintModel {
    String token;
    int complaint_category_id_fk;
    int district_id_fk;
    String complaint_council;
    String complaint_detail;
    List<File> upload_file;

    public ComplaintModel() {
    }

    public ComplaintModel(String token, int complaint_category_id_fk, int district_id_fk, String complaint_council, String complaint_detail, List<File> upload_file) {
        this.token = token;
        this.complaint_category_id_fk = complaint_category_id_fk;
        this.district_id_fk = district_id_fk;
        this.complaint_council = complaint_council;
        this.complaint_detail = complaint_detail;
        this.upload_file = upload_file;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getComplaint_category_id_fk() {
        return complaint_category_id_fk;
    }

    public void setComplaint_category_id_fk(int complaint_category_id_fk) {
        this.complaint_category_id_fk = complaint_category_id_fk;
    }

    public int getDistrict_id_fk() {
        return district_id_fk;
    }

    public void setDistrict_id_fk(int district_id_fk) {
        this.district_id_fk = district_id_fk;
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

    public List<File> getUpload_file() {
        return upload_file;
    }

    public void setUpload_file(List<File> upload_file) {
        this.upload_file = upload_file;
    }
}
