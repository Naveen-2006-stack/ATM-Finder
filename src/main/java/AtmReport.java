import java.sql.Timestamp;

/**
 * Simple data holder for the latest ATM report.
 */
public class AtmReport {
    private final String cashStatus;
    private final String depositStatus;
    private final String passbookStatus;
    private final Timestamp reportTimestamp;

    public AtmReport(String cashStatus, String depositStatus, String passbookStatus, Timestamp reportTimestamp) {
        this.cashStatus = cashStatus;
        this.depositStatus = depositStatus;
        this.passbookStatus = passbookStatus;
        this.reportTimestamp = reportTimestamp;
    }

    public String getCashStatus() { return cashStatus; }
    public String getDepositStatus() { return depositStatus; }
    public String getPassbookStatus() { return passbookStatus; }
    public Timestamp getReportTimestamp() { return reportTimestamp; }
}
