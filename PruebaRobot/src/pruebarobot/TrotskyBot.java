package pruebarobot;
import robocode.*;
import java.util.Random;
import java.awt.Color;
import robocode.util.Utils;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * PruebaRobot - a robot by Trotsky
 */
public class TrotskyBot extends Robot
{
    /**
     * run: PruebaRobot's default behavior
     */
    int direccionInicial;
    double robotSize[] = new double[2];//0 -> x - 1 -> y
    double backSize[] = new double[2];
    int corner = 0; //0 1 2 3
    Modo modoRobot = Modo.movimiento;
    double ang = 50; //angulo de rotacion del arma
    int p = 1;//potencia del disparo
    public void run() {
        robotSize[0] = getWidth();
        robotSize[1] = getHeight();
        backSize[0] = getBattleFieldWidth();
        backSize[1] = getBattleFieldHeight();
        setColors(Color.ORANGE, Color.GREEN, Color.RED);
        movimientoInicial();
        while(true) {
            if (modoRobot == Modo.movimiento) { 
                mover();
            } else if (modoRobot == Modo.attack) {
                atacar();
            }
        }
    }
    
    boolean movInicFinalizado = false;
    public void movimientoInicial() {
        //Random rnd = new Random();
        //direccionInicial = rnd.nextInt() % 2;
        direccionInicial = 0;
        if (direccionInicial == 0) {
            turnLeft(getHeading());
        }/* else {
            if (getHeading() > 180) {
                turnLeft(getHeading()-180);
            } else {
                turnRight(180-getHeading());
            }
        }*/
        turnGunLeft(90);//Apunta para abajo
        ahead(backSize[1]);//Va a la pared de arriba
        movInicFinalizado = true;
    }
    
    int retries = 10; //Cantidad de turnos que el robot va a seguir girando el cosito
    double angI;
    boolean virginity = true;
    public void atacar() {
        if (virginity){
            angI = getGunHeading();
            virginity = false;
        }
        turnGunLeft(ang);
        ang *= -1;
        retries--;
        if (retries <= 0) {
            modoRobot = Modo.movimiento;
            retries = 10;
            //arreglar0
            virginity = true;
            turnGunLeft(-getHeading()+angI+90);
        }
    }
    
    double distReg;//Guarda la posicion del robot cuando se produjo la interrupcion
    boolean interrumpido = false;//Indica si el robot tuvo que parar para atacar
    public void mover(){
            switch(corner) {
                case 0: //arriba izquierda
                    cambiarDeEsquina(distReg, 0);
                    break;
                case 1: //abajo izquierda
                    cambiarDeEsquina(distReg, 1);
                    break;
                case 2: //abajo derecha
                    cambiarDeEsquina(backSize[0]-distReg, 0);
                    break;
                default: //arriba derecha
                    cambiarDeEsquina(backSize[1]-distReg, 1);
                    break;
            }
            out.println("Yendo al corner " + corner);
            out.println("Posicion del robot: " + getX() + " - " + getY());
            out.println("Tamaño del robot: " + robotSize[0] + " - " + robotSize[1]);
            out.println("Tamaño del escenario: " + backSize[0] + " - " + backSize[1]);
            
            
            //las 4 condiciones magicas
            boolean c1 = (getX() == robotSize[0]/2 && getY() == backSize[1]-robotSize[1]/2);
            boolean c2 = (getX() == robotSize[0]/2 && getY() == robotSize[1]/2);
            boolean c3 = (getX() == backSize[0]-robotSize[0]/2 && getY() == robotSize[1]/2);
            boolean c4 = (getX() == backSize[0]-robotSize[0]/2 && getY() == backSize[1]-robotSize[1]/2);
            out.println(c1 + " " + c2 + " " + c3 + " " + c4);
            
            //Evitar que pegue vueltas locas sin que nadie se lo pida
            /*if (c1 || c2 || c3 || c4) {
                corner = corner < 3? corner+1 : 0;
                interrumpido = false;
            }*/
            if (c1) {
                corner = 1;
                interrumpido = false;
            } else if (c2) {
                corner = 2;
                interrumpido = false;
            } else if (c3) {
                corner = 3;
                interrumpido = false;
            } else if (c4) {
                corner = 0;
                interrumpido = false;
            }
            
    }
    
    //distInt -> distancia a mover en caso de interrupcion
    //eje 0 -> x // eje 1 -> y
    void cambiarDeEsquina(double distInt, int eje) {
        if (interrumpido) {
            ahead(distInt);
        } else {
            turnLeft(90);
            ahead(backSize[eje]);
        }
    }

    /**
     * onScannedRobot: What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        fire(p);
    }

    public void onBulletHit(BulletHitEvent e) {
        p++;
        if (modoRobot != Modo.attack && movInicFinalizado) {
            modoRobot = Modo.attack;
            interrumpido = true;
            if (corner == 2 || corner == 0) {
                distReg = getX();
            } else if (corner == 1 || corner == 3) {
                distReg = getY();
            }
            stop();
        } else {
            retries++;//Si la bala impacta se le da otra oportunidad
        }
    }
    
    public void onBulletMissed(BulletMissedEvent e) {
        if (p > 1) {
            p--;
        }
    }
    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    public void onHitByBullet(HitByBulletEvent e) {
        //
    }

    /**
     * onHitWall: What to do when you hit a wall
     */
    public void onHitWall(HitWallEvent e) {
        
    }
    
    public void onHitRobot(HitRobotEvent e) {
        
    }
    
}
