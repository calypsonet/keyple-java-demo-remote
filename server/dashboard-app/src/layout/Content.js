import React from 'react';
import PropTypes from 'prop-types';
import { DataGrid } from '@material-ui/data-grid';
import Paper from '@material-ui/core/Paper';
import { withStyles } from '@material-ui/core/styles';

const styles = (theme) => ({
  paper: {
    margin: 'auto',
    overflow: 'hidden',
  }
});


const columns = [
  { field: 'id', headerName: 'ID', width: 70 },
  { field: 'cardSn', headerName: 'Card S/N', width: 150 },
  { field: 'deviceId', headerName: 'Device', width: 100 },
  { field: 'plugin', headerName: 'Plugin', width: 100 },
  { field: 'timestamp', headerName: 'Started at', width: 150},
  { field: 'type', headerName: 'Type', width: 150 },
  {
    field: 'status',
    headerName: 'Status',
    width: 100,
  },
  {
    field: 'contract',
    headerName: 'Contract loaded',
    description: 'This column has a value getter and is not sortable.',
    sortable: false,
    width: 160,
    /*valueGetter: (params) =>
    `${params.getValue('type') || ''} ${params.getValue('cardSn') || ''}`,*/
  },
];

const rows = [
  { id: '2101',timestamp : new Date(),deviceId:'x23d45F',plugin:'NFC', type: 'RELOAD',    cardSn: 'D4AAA0203A2', status :'SUCCESS', contract: 'SEASON_PASS' },
  { id: '2102',timestamp : new Date(),deviceId:'x23d45F',plugin:'OMAPI', type: 'ISSUANCE',  cardSn: 'D4AAA0203A2', status :'SUCCESS',contract: ''},
  { id: '2103',timestamp : new Date(),deviceId:'x23d45F',plugin:'NFC', type: 'RELOAD',    cardSn: 'D4AAA0203A2', status :'SUCCESS',contract: 'SEASON_PASS' },
  { id: '2104',timestamp : new Date(),deviceId:'x23d45F',plugin:'NFC', type: 'SECURE READ', cardSn: 'D4AAA0203A2', status :'SUCCESS',contract: '' },
  { id: '2105',timestamp : new Date(),deviceId:'x23d45F',plugin:'NFC', type: 'SECURE READ', cardSn: 'D4AAA0203A2', status :'SUCCESS',contract: '' },
  { id: '2106',timestamp : new Date(),deviceId:'x23d45F',plugin:'NFC', type: 'RELOAD', cardSn: 'D4AAA0203A2', status :'SUCCESS',contract: 'MULTI-TRIP : 10' },
  { id: '2107',timestamp : new Date(),deviceId:'x23d45F',plugin:'WIZWAY', type: 'RELOAD', cardSn: 'D4AAA0203A2', status :'SUCCESS',contract: 'MULTI-TRIP : 1' },
  { id: '2108',timestamp : new Date(),deviceId:'x23d45F',plugin:'NFC', type: 'RELOAD', cardSn: 'D4AAA0203A2', status :'FAILED',contract: 'MULTI-TRIP : 10' },
  { id: '2109',timestamp : new Date(),deviceId:'x23d45F',plugin:'NFC', type: 'RELOAD', cardSn: 'D4AAA0203A2', status :'SUCCESS',contract: 'MULTI-TRIP : 10' },
];

function Content(props) {
  const { classes } = props;

  return (
    <Paper className={classes.paper}>
      <div style={{ height: 800, width: '100%' }}>
        <DataGrid rows={rows} columns={columns} pageSize={10}  />
      </div>
    </Paper>
  );
}

Content.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Content);