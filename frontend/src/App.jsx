import { useState } from 'react';
import './App.css';

function App() {
  const [pincode, setPincode] = useState('');
  const [atms, setAtms] = useState([]);
  const [selectedAtm, setSelectedAtm] = useState(null);
  const [reportData, setReportData] = useState({
    cashStatus: 'WORKING',
    depositStatus: 'NOT_AVAILABLE',
    passbookStatus: 'NOT_AVAILABLE',
  });

  const handleSearch = async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/atms?pincode=${pincode}`);
      const data = await response.json();
      setAtms(data);
      setSelectedAtm(null);
    } catch (error) {
      console.error('Error fetching ATMs:', error);
    }
  };

  const handleSubmitReport = async () => {
    if (!selectedAtm) return;
    const { name, address } = selectedAtm;
    const reportPayload = {
      name,
      address,
      ...reportData,
    };
    try {
      await fetch('http://localhost:8080/api/report', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(reportPayload),
      });
      alert('Report Submitted!');
    } catch (error) {
      console.error('Error submitting report:', error);
    }
  };

  const handleReportFormChange = (e) => {
    const { name, value } = e.target;
    setReportData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  return (
    <div className="App">
      <header className="app-header">
        <h1>ATM Finder</h1>
      </header>
      <div className="search-panel">
        <input
          type="text"
          value={pincode}
          onChange={(e) => setPincode(e.target.value)}
          placeholder="Enter pincode"
        />
        <button onClick={handleSearch}>Search</button>
      </div>
      <div className="main-content">
        <div className="results-panel">
          <h2>Results</h2>
          {atms.map((atm) => (
            <div
              className="atm-card"
              key={atm.address}
              onClick={() => setSelectedAtm(atm)}
            >
              <div
                className={`card-status-dot ${
                  atm.latestReport?.cashStatus === 'OUT_OF_CASH'
                    ? 'status-red'
                    : 'status-green'
                }`}
              ></div>
              <div className="card-info">
                <h4>{atm.name}</h4>
                <p>{atm.address}</p>
              </div>
            </div>
          ))}
        </div>
        <div className="details-panel">
          {selectedAtm ? (
            <div>
              <h2>Details & Report</h2>
              <h3>{selectedAtm.name}</h3>
              <p>{selectedAtm.address}</p>
              <div className="last-report">
                {selectedAtm.latestReport ? (
                  <>
                    <p>Cash Status: {selectedAtm.latestReport.cashStatus}</p>
                    <p>Deposit Status: {selectedAtm.latestReport.depositStatus}</p>
                    <p>Passbook Status: {selectedAtm.latestReport.passbookStatus}</p>
                  </>
                ) : (
                  <p>No reports for this ATM yet.</p>
                )}
              </div>
              <form>
                <h3>Submit a New Report</h3>
                <label>
                  Cash Status:
                  <select
                    name="cashStatus"
                    value={reportData.cashStatus}
                    onChange={handleReportFormChange}
                  >
                    <option value="WORKING">Working</option>
                    <option value="NOT_WORKING">Not Working</option>
                    <option value="OUT_OF_CASH">Out of Cash</option>
                  </select>
                </label>
                <label>
                  Deposit Status:
                  <select
                    name="depositStatus"
                    value={reportData.depositStatus}
                    onChange={handleReportFormChange}
                  >
                    <option value="AVAILABLE">Available</option>
                    <option value="NOT_AVAILABLE">Not Available</option>
                  </select>
                </label>
                <label>
                  Passbook Status:
                  <select
                    name="passbookStatus"
                    value={reportData.passbookStatus}
                    onChange={handleReportFormChange}
                  >
                    <option value="AVAILABLE">Available</option>
                    <option value="NOT_AVAILABLE">Not Available</option>
                  </select>
                </label>
                <button type="button" onClick={handleSubmitReport}>
                  Submit Report
                </button>
              </form>
            </div>
          ) : (
            <div>Select an ATM from the results to view or report status.</div>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;
