/**
 * Simple DTO for incoming report JSON posted to the API.
 * Fields are public to keep Gson parsing straightforward.
 */
public class AtmReportData {
    public String name;
    public String address;
    public String cashStatus;
    public String depositStatus;
    public String passbookStatus;

    public AtmReportData() {}
}
