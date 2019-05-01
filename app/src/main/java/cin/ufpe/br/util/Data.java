package cin.ufpe.br.util;

public class Data {

    private boolean wasOffloaded;
    private String result = "";
    private int name = 0;
    private int faces = 0;
    private String size = "";
    private String totalTime = ""; //Total time
    private long uploadTime = 0;
    private long downloadTime = 0;
    private String algorithm = "";
    private String execution = "";
    private String bandwidth = "";
    private String CPUSmart = "";
    private String CPUNuvem = "";
    private String time = "";

    public void setWasOffloaded(boolean wasOffloaded) {
        this.wasOffloaded = wasOffloaded;
    }

    public String getData() {
        return result;
    }

    public void setResult() {
        result += "\"" + name + "\";\"" + time + "\";\"" +
                faces + "\";\"" + algorithm + "\";\"" +
                execution + "\";\"" + wasOffloaded + "\";\"" +
                size + "\";\"" + totalTime + "\";\"" + uploadTime + "\";\"" +
                downloadTime + "\";\"" + bandwidth + "\";\"" + CPUSmart + "\";\"" + CPUNuvem + "\"";
        result += "\n";
    }

    public void setName(int name) {
        this.name = name;
    }

    public void setFaces(int faces) {
        this.faces = faces;
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

    public void setBandwidth(String bandwidth) {
        this.bandwidth = bandwidth;
    }

    public void setCPUSmart(String CPUSmart) {
        this.CPUSmart = CPUSmart;
    }

    public void setCPUNuvem(String CPUNuvem) {
        this.CPUNuvem = CPUNuvem;
    }
}

