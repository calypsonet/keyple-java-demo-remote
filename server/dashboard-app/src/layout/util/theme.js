import background from '../../img/background.png';
import { createMuiTheme, ThemeProvider, withStyles } from '@material-ui/core/styles';

//Create base Theme with Material UI
let muitheme = createMuiTheme({
  palette: {
    primary: {
      //light: '#63ccff',
      //main: '#1A87C7',
      main: '#fff',
      //dark: '#006db3',
    },
  },
  typography: {
    fontFamily:'Work Sans',
    h5: {
      fontWeight: 500,
      fontSize: 26,
      letterSpacing: 0.5,
    },
  },
  shape: {
    borderRadius: 8,
  },
  props: {
    MuiTab: {
      disableRipple: true,
    },
  },
  mixins: {
    toolbar: {
      minHeight: 48,
    },
  },
});

//Add more style to theme
export const theme = {
  ...muitheme,
  overrides: {
    MuiDrawer: {
      paper: {
        //backgroundColor: '#18202c',
        //backgroundColor: '#000000',
        backgroundColor: '#1A87C7',
      },
    },
    MuiButton: {
      label: {
        textTransform: 'none',
      },
      contained: {
        boxShadow: 'none',
        '&:active': {
          boxShadow: 'none',
        },
      },
    },
    MuiTabs: {
      root: {
        marginLeft: muitheme.spacing(1),
      },
      indicator: {
        height: 3,
        borderTopLeftRadius: 3,
        borderTopRightRadius: 3,
        backgroundColor: muitheme.palette.common.white,
      },
    },
    MuiTab: {
      root: {
        textTransform: 'none',
        margin: '0 16px',
        minWidth: 0,
        padding: 0,
        [muitheme.breakpoints.up('md')]: {
          padding: 0,
          minWidth: 0,
        },
      },
    },
    MuiIconButton: {
      root: {
        padding: muitheme.spacing(1),
      },
    },
    MuiTooltip: {
      tooltip: {
        borderRadius: 4,
      },
    },
    MuiDivider: {
      root: {
        //backgroundColor: '#404854',
        backgroundColor: '#fff',
      },
    },
    MuiListItemText: {
      primary: {
        fontWeight: muitheme.typography.fontWeightMedium,
      },
    },
    MuiListItemIcon: {
      root: {
        color: 'inherit',
        marginRight: 0,
        '& svg': {
          fontSize: 20,
        },
      },
    },
    MuiAvatar: {
      root: {
        width: 32,
        height: 32,
      },
    },
  },
};

export const drawerWidth = 200;

export const styles = {
  root: {
    display: 'flex',
    minHeight: '100vh',
  },
  drawer: {
    [muitheme.breakpoints.up('sm')]: {
      width: drawerWidth,
      flexShrink: 0,
    },
  },
  app: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
  },
  main: {
    flex: 1,
    padding: muitheme.spacing(3, 4),
    background: '#fff',
    backgroundImage: `url(${background})`,
    backgroundRepeat: 'no-repeat',
    backgroundSize: 'cover',
  },
  footer: {
    padding: muitheme.spacing(2),
    background: '#fff'
  },
};