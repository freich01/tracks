package com.example.myapplicationsddddddddddddddd;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;
import java.util.List;

public class Exercise implements Parcelable {

    @DocumentId
    private String id;
    private String name;
    private List<String> weights;
    private List<String> reps;
    private String notes;
    private Date dateLastTrained;
    private String workoutPlanId;
    private boolean multiTrain;

    public Exercise() {
        // needed for Firebase
    }

    public Exercise(String id, String name, List<String> weights, List<String> reps, String notes, Date dateLastTrained, String workoutPlanId, boolean multiTrain) {
        this.id = id;
        this.name = name;
        this.weights = weights;
        this.reps = reps;
        this.notes = notes;
        this.dateLastTrained = dateLastTrained;
        this.workoutPlanId = workoutPlanId;
        this.multiTrain = multiTrain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exercise exercise = (Exercise) o;
        return id != null && id.equals(exercise.id);
    }

    // reconstructs object from parcel
    protected Exercise(Parcel in) {
        id = in.readString();
        name = in.readString();
        weights = in.createStringArrayList();
        reps = in.createStringArrayList();
        notes = in.readString();
        dateLastTrained = (Date) in.readSerializable();
        workoutPlanId = in.readString();
        multiTrain = in.readByte() != 0;
    }

    // writes an object`s data to a parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeStringList(weights);
        dest.writeStringList(reps);
        dest.writeString(notes);
        dest.writeSerializable(dateLastTrained);
        dest.writeString(workoutPlanId);
        dest.writeByte((byte) (multiTrain ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Exercise> CREATOR = new Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getWeights() {
        return weights;
    }

    public void setWeights(List<String> weights) {
        this.weights = weights;
    }

    public List<String> getReps() {
        return reps;
    }

    public void setReps(List<String> reps) {
        this.reps = reps;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getDateLastTrained() {
        return dateLastTrained;
    }

    public void setDateLastTrained(Date dateLastTrained) {
        this.dateLastTrained = dateLastTrained;
    }

    public String getWorkoutPlanId() {
        return workoutPlanId;
    }

    public void setWorkoutPlanId(String workoutPlanId) {
        this.workoutPlanId = workoutPlanId;
    }

    public boolean isMultiTrain() {
        return multiTrain;
    }

    public void setMultiTrain(boolean multiTrain) {
        this.multiTrain = multiTrain;
    }
}



