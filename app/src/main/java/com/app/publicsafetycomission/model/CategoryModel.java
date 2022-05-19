package com.app.publicsafetycomission.model;

public class CategoryModel {
    String complaint_category_id;
    String complaint_category_name;

    public CategoryModel() {
    }

    public CategoryModel(String complaint_category_id, String complaint_category_name) {
        this.complaint_category_id = complaint_category_id;
        this.complaint_category_name = complaint_category_name;
    }

    public String getComplaint_category_id() {
        return complaint_category_id;
    }

    public void setComplaint_category_id(String complaint_category_id) {
        this.complaint_category_id = complaint_category_id;
    }

    public String getComplaint_category_name() {
        return complaint_category_name;
    }

    public void setComplaint_category_name(String complaint_category_name) {
        this.complaint_category_name = complaint_category_name;
    }
}
