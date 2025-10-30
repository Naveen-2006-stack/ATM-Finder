/**
 * Represents an ATM location with its address and the latest local report (may be null).
 */
public class AtmLocation {
    private final String name;
    private final String address;
    private final AtmReport latestReport;

    public AtmLocation(String name, String address, AtmReport latestReport) {
        this.name = name;
        this.address = address;
        this.latestReport = latestReport;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }
    public AtmReport getLatestReport() { return latestReport; }
}
