package br.ufpe.cin.mpos.profile.Model;

/**
 * Created by eduar on 30/01/2017.
 */

public class Model {
    public int IDC;
    public String Tech;
    public String AppName;
    public String Carrier;
    public String Battery;
    public String year;
    public String CPU;
    public String SizeInput;
    public String Bandwidth;
    public String RSSI;
    public String CPUNuvem;
    public String Date;

    public String toString() {
        return IDC + " " + Tech + " " + AppName + " " + Carrier + " " + Battery + " " + year + " " + CPU + " " + SizeInput +" "+ Bandwidth + " " + RSSI + " " + CPUNuvem + " " + Date;
    }

}
