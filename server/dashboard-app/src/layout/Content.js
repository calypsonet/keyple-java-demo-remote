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
 /*
  */
 { field: 'id', headerName: 'ID', width: 70     ,sortable: false
  },
  { field: 'startedAt', headerName: 'Started at', width: 140},
  {
    field: 'status',
    headerName: 'Status',
    width: 100,
  },
  { field: 'type', headerName: 'Action', width: 120,sortable: false },
  { field: 'poSn', headerName: 'Card S/N', width: 170 ,sortable: false},
  // { field: 'deviceId', headerName: 'Device', width: 100 },
  { field: 'plugin', headerName: 'Plugin', width: 150 ,sortable: false},
  {
    field: 'contractLoaded',
    headerName: 'Contract loaded',
    description: 'This column has a value getter and is not sortable.',
    sortable: false,
    width: 200,
    /*valueGetter: (params) =>
    `${params.getValue('type') || ''} ${params.getValue('poSn') || ''}`,*/
  },
];

 const rows_init = [
 { id: '23c4',startedAt : '23/02/21 10:32',deviceId:'x23d45F',plugin:'Android NFC', type: 'RELOAD',    poSn: '00000000C16B293E', status :'SUCCESS', contractLoaded: 'SEASON_PASS' },
 { id: '998a',startedAt : '23/02/21 10:31',deviceId:'x23d45F',plugin:'Android OMAPI', type: 'ISSUANCE',  poSn: '00000000C16B293E', status :'ERROR',contractLoaded: ''},
 { id: 'b2b2',startedAt : '23/02/21 10:31',deviceId:'x23d45F',plugin:'Android Wizway', type: 'RELOAD', poSn: '00000000C16B293E ', status :'SUCCESS',contractLoaded: 'MULTI-TRIP : 10' }
 ];

/*
const rows_init = []
 */


function Content(props) {
  const { classes } = props;
  const [rows, setRows] = useState(rows_init);

  // subscribe to transaction notification
  useEffect(() => {
    subscribeTransactionWait()
  });


  const addRow = transaction => {
    console.log("adding a new row to table")
    setRows(rows =>[
      transaction,
      ...rows
    ]);
  };


  const subscribeTransactionWait = async function () {
    try {
      //let response = await fetch(process.env.REACT_APP_API_URL+"/dashboard/transaction/wait");
      let response = await fetch("/dashboard/transaction/wait");
      if (response.status === 204) {
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
      <div style={{ height: 450, width: '100%'}}>

        <DataGrid rows={rows} columns={columns} pageSize={8}  />



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