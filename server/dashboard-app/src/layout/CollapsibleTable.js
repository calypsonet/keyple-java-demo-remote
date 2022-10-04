import React, { useState, useEffect }  from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import IconButton from '@material-ui/core/IconButton';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import KeyboardArrowDownIcon from '@material-ui/icons/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@material-ui/icons/KeyboardArrowUp';

const useRowStyles = makeStyles({
  root: {
    '& > *': {
      borderBottom: 'unset',
    },
  },
});

function Row(props) {
  const { row,lastRowId } = props;
  const [open, setOpen] = React.useState(false);
  const classes = useRowStyles();


  return (
    <React.Fragment>
      <TableRow className={row.id === lastRowId ? (row.status === "SUCCESS" ? `newRowSuccess`:`newRowError`)  : classes.root} key={row.id}>
        <TableCell align="center">{row.id}</TableCell>
        <TableCell align="center">{row.startedAt}</TableCell>
        <TableCell align="center">{row.plugin}</TableCell>
        <TableCell align="center">{row.type}</TableCell>
        <TableCell align="center">{row.cardSerialNumber}</TableCell>
        <TableCell align="center">{row.status}</TableCell>
        <TableCell align="center">{row.contractLoaded}</TableCell>
      </TableRow>
      {/*
      uncomment to include more information
      <TableRow>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={6}>
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Box margin={1}>
              <Typography variant="h6" gutterBottom component="div">
                History
              </Typography>
              <Table size="small" aria-label="purchases">
                <TableHead>
                  <TableRow>
                    <TableCell>Date</TableCell>
                    <TableCell>Customer</TableCell>
                    <TableCell align="right">Amount</TableCell>
                    <TableCell align="right">Total price ($)</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {row.history.map((historyRow) => (
                    <TableRow key={historyRow.date}>
                      <TableCell component="th" scope="row">
                        {historyRow.date}
                      </TableCell>
                      <TableCell>{historyRow.customerId}</TableCell>
                      <TableCell align="right">{historyRow.amount}</TableCell>
                      <TableCell align="right">
                        {Math.round(historyRow.amount * row.price * 100) / 100}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>*/}
    </React.Fragment>
  );
}

Row.propTypes = {
  row: PropTypes.shape({
    id: PropTypes.string.isRequired,
    startedAt: PropTypes.string.isRequired,
    /*history: PropTypes.arrayOf(
      PropTypes.shape({
        amount: PropTypes.number.isRequired,
        customerId: PropTypes.string.isRequired,
        date: PropTypes.string.isRequired,
      }),
    ),*/
    plugin: PropTypes.string.isRequired,
    type: PropTypes.string.isRequired,
    cardSerialNumber: PropTypes.string.isRequired,
    status: PropTypes.string.isRequired,
  }).isRequired,
};

export default function CollapsibleTable(props) {
  const {rows, lastRowId} = props;

  return (
    <TableContainer component={Paper}>
      <Table aria-label="collapsible table">
        <TableHead>
          <TableRow>
            <TableCell align="center">ID</TableCell>
            <TableCell align="center">Started At</TableCell>
            <TableCell align="center">Plugin</TableCell>
            <TableCell align="center">Action</TableCell>
            <TableCell align="center">Card S/N</TableCell>
            <TableCell align="center">Status</TableCell>
            <TableCell align="center">Contract Loaded</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.length>0 ?
            rows.map((row) => (<Row key={row.name} row={row} lastRowId={lastRowId} />))
            : <TableRow key="empty-row"><TableCell colSpan={6}>No transaction has been processed yet</TableCell></TableRow>}

        </TableBody>
      </Table>
    </TableContainer>
  );
}