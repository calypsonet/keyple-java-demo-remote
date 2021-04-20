import Typography from '@material-ui/core/Typography';
import Link from '@material-ui/core/Link';
import logo_eclipse_fondation from '../img/logo_eclipse_fondation_black.png';

 export default function Copyright() {
  return (
    <div>
      <div align="center">
        <img src={logo_eclipse_fondation} width="100px" id="logo-eclipse-fondation"/>
      </div>
      <Typography variant="body2" color="textSecondary" align="center">
        {'Eclipse Keyple © '}
        <Link color="inherit">
          Calypso Network Association
        </Link>{' '}
        {new Date().getFullYear()}
        {'.'}
      </Typography>
    </div>
  );
}