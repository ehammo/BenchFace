package cin.ufpe.br.Util;

public class Data {

    private String result = "";
    private int name = 0;
    private int faces = 0;
    private String oRes = ""; //Original Resolution
    private String pRes = ""; //Processed Resolution
    private String size = "";
    private String totalTime = ""; //Total time
    private long cpuTime = 0;
    private long uploadTime = 0;
    private long downloadTime = 0;
    private String algorithm = "";
    private String execution = "";
    private String time = "";

    public String getData() {
        return result;
    }

    public void setResult() {
        result += "\"" + name + "\",\"" + faces + "\",\"" + algorithm + "\",\"" + execution + "\",\"" + oRes + "\", \"" + pRes + "\", \"" + size + "\", \"" + totalTime + "\", \"" + cpuTime + "\", \"" + uploadTime + "\", \"" + downloadTime + "\", \"" + time + "\"";
        result += "\n";
    }

    public void setName(int name) {
        this.name = name;
    }

    public void setFaces(int faces) {
        this.faces = faces;
    }

    public void setoRes(String oRes) {
        this.oRes = oRes;
    }

    public void setpRes(String pRes) {
        this.pRes = pRes;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public void setDownloadTime(long downloadTime) {
        this.downloadTime = downloadTime;
    }

    public void setCpuTime(long cpuTime) {
        this.cpuTime = cpuTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setExecution(String execution) {
        this.execution = execution;
    }

    public void setTime(String time) {
        this.time = time;
    }
}