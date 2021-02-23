import React, { useState, useEffect }  from 'react';
import PropTypes from 'prop-types';
import { DataGrid } from '@material-ui/data-grid';
import Paper from '@material-ui/core/Paper';
import { withStyles } from '@material-ui/core/styles';




const styles = (theme) => ({
  paper: {
    margin: 'auto',
    overflow: 'hidden',
    background: '#FFF',
  }
});


const columns = [
  { field: 'id', headerName: 'ID', width: 70     ,sortable: false
  },
  { field: 'startedAt', headerName: 'Started at', width: 200},
  {
    field: 'status',
    headerName: 'Status',
    width: 100,
  },
  { field: 'poSn', headerName: 'Card S/N', width: 200 ,sortable: false},
  // { field: 'deviceId', headerName: 'Device', width: 100 },
  { field: 'plugin', headerName: 'Plugin', width: 150 ,sortable: false},
  { field: 'type', headerName: 'Type', width: 200,sortable: false },
  {
    field: 'contract',
    headerName: 'Contract loaded',
    description: 'This column has a value getter and is not sortable.',
    sortable: false,
    width: 200,
    /*valueGetter: (params) =>
    `${params.getValue('type') || ''} ${params.getValue('poSn') || ''}`,*/
  },
];

 const rows_init = [
 { id: '2101',startedAt : '2021-02-17 12:00:21',deviceId:'x23d45F',plugin:'NFC', type: 'RELOAD',    poSn: 'D4AAA0203A2', status :'SUCCESS', contract: 'SEASON_PASS' },
 { id: '2102',startedAt : '2021-02-17 12:01:11',deviceId:'x23d45F',plugin:'OMAPI', type: 'ISSUANCE',  poSn: 'D4AAA0203A2', status :'SUCCESS',contract: ''},
 { id: '2106',startedAt : '2021-02-17 12:05:55',deviceId:'x23d45F',plugin:'NFC', type: 'RELOAD', poSn: 'D4AAA0203A2', status :'SUCCESS',contract: 'MULTI-TRIP : 10' }
 ];

/*
const rows_init = []
 */


function Content(props) {
  const { classes } = props;
  const [rows, setRows] = useState(rows_init);

  // Simiar to componentDidMount and componentDidUpdate
  // Update
  useEffect(() => {
    subscribeTransactionWait()
  });


  const addRow = transaction => {
    console.log("adding a new row to table")
    setRows(rows =>[
      ...rows,
      transaction
    ]);
  };


  const subscribeTransactionWait = async function () {
    try {
      let response = await fetch(process.env.REACT_APP_API_URL+"/dashboard/transaction/wait");
      if (response.status === 502) {
        console.log("Received a Connection Timeout Error response from server: " + response.status)
        await subscribeTransactionWait();
      } else if (response.status === 204) {
        console.log("Received a No-Content response from server: " + response.status)
        await subscribeTransactionWait();
      } else if (response.status === 200) {
        // Received a new transaction
        let message = await response.text();
        console.log("Received a transaction from server: " + message)
        addRow(JSON.parse(message))
        await subscribeTransactionWait();
      } else {
        //unexpected error connect again
        await subscribeTransactionWait();
      }
    }catch(e){
      console.log("Error while connection to server : "+ e)
      setTimeout(subscribeTransactionWait, 5000)
    }
  }


  return (
    <Paper className={classes.paper}>
      <div style={{ height: 400, width: '100%'}}>

        <DataGrid rows={rows} columns={columns} pageSize={5}  />
      </div>
      {/*<div>
        <form onSubmit={addRow}>
          <input
            name="item"
            type="text"
            value="2"
          />
          <button>Submit</button>
        </form>
      </div>*/}
    </Paper>
  );
}

Content.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Content);