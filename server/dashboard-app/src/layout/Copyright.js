import Typography from '@material-ui/core/Typography';
import Link from '@material-ui/core/Link';
import logo_cna from '../img/logo-cna.png';

 export default function Copyright() {
  return (
    <div>
      <div align="center">
        <img src={logo_cna} width="100px" id="logo-eclipse-fondation"/>
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