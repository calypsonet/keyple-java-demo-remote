import React, { useState, useEffect,componentDidMount }  from 'react';
import PropTypes from 'prop-types';
import AppBar from '@material-ui/core/AppBar';
import Avatar from '@material-ui/core/Avatar';
import Grid from '@material-ui/core/Grid';
import Hidden from '@material-ui/core/Hidden';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import Toolbar from '@material-ui/core/Toolbar';

import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import AlertDialog from './AlertDialog'
const lightColor = 'rgba(255, 255, 255, 0.7)';

const styles = (theme) => ({
  secondaryBar: {
    zIndex: 0,
  },
  menuButton: {
    marginLeft: -theme.spacing(1),
  },
  iconButtonAvatar: {
    padding: 4,
  },
  link: {
    textDecoration: 'none',
    color: lightColor,
    '&:hover': {
      color: theme.palette.common.white,
    },
  },
  button: {
    borderColor: lightColor,
  },
});

/**
 * Header Component
 * @param props
 * @returns {XML}
 * @constructor
 */
function Header(props) {
  const { classes, onDrawerToggle,isSamReady,isServerReady } = props;
    return (
    <React.Fragment>
      <AlertDialog show={isServerReady && !isSamReady} title="SAM Resource is not available" text="Please ensure that the SAM is inserted into the SAM Reader"/>
      <AlertDialog show={!isServerReady} title="Can not reach Keyple Distributed Server" text="Keyple Distributed server is not reachable. Please ensure that the Server process is running. Restart it if needed."/>
      <AppBar color="primary" position="sticky" elevation={0}>
        <Toolbar>
          <Grid container spacing={1} alignItems="center">
            <Hidden smUp>
              <Grid item>
                <IconButton
                  color="inherit"
                  aria-label="open drawer"
                  onClick={onDrawerToggle}
                  className={classes.menuButton}
                >
                  <MenuIcon />
                </IconButton>
              </Grid>
            </Hidden>
            <Grid item xs />
            <Grid item>
              <Typography>
                {props.isSamReady?"Sam Resource is ready":"Sam Resource is NOT Ready"}
              </Typography>
            </Grid>
            <Grid item>
              <IconButton color="inherit" className={classes.iconButtonAvatar}>
                <Avatar alt="My Avatar" />
              </IconButton>
            </Grid>
          </Grid>
        </Toolbar>
      </AppBar>
      <AppBar
        component="div"
        className={classes.secondaryBar}
        color="primary"
        position="static"
        elevation={0}
      >
        <Toolbar>
          <Grid container spacing={2} alignItems="center" >
            <Grid item>
            </Grid>
            <Grid item >
              <Typography color="inherit" variant="h5" component="h1">
                Ticketing Transactions
              </Typography>
            </Grid>
          </Grid>
        </Toolbar>
      </AppBar>
    </React.Fragment>
  );
}

Header.propTypes = {
  classes: PropTypes.object.isRequired,
  onDrawerToggle: PropTypes.func.isRequired,
  isSamReady: PropTypes.bool,
  isServerReady: PropTypes.bool
};

export default withStyles(styles)(Header);