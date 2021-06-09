package docmate.model;

public class PatientMedicine {

    private int prescriptionId;
    private int medicineNumber;
    private int medicineId;
    private String dosageForm;
    private String brandName;
    private String strength;
    private String duration;
    private String dosage;
    private String advice;

    public PatientMedicine() {
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public int getMedicineNumber() {
        return medicineNumber;
    }

    public void setMedicineNumber(int medicineNumber) {
        this.medicineNumber = medicineNumber;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    @Override
    public String toString() {
        return "PatientMedicine{" +
                "prescriptionId=" + prescriptionId +
                ", medicineNumber=" + medicineNumber +
                ", medicineId=" + medicineId +
                ", dosageForm='" + dosageForm + '\'' +
                ", brandName='" + brandName + '\'' +
                ", strength='" + strength + '\'' +
                ", duration='" + duration + '\'' +
                ", dosages='" + dosage + '\'' +
                ", advice='" + advice + '\'' +
                '}';
    }
}

