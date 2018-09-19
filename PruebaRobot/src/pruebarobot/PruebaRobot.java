package pruebarobot;
import robocode.*;
import java.util.Random;
import java.awt.Color;
import robocode.util.Utils;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * PruebaRobot - a robot by Trotsky
 */
//enum Modo {movimiento, attack};
public class PruebaRobot extends Robot
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
    
    public void movimientoInicial() {
        //Random rnd = new Random();
        //direccionInicial = rnd.nextInt() % 2;
        direccionInicial = 0;
        if (direccionInicial == 0) {
            turnLeft(getHeading());
        } else {
            if (getHeading() > 180) {
                turnLeft(getHeading()-180);
            } else {
                turnRight(180-getHeading());
            }
        }
        turnGunLeft(90);//Apunta para abajo
        out.println(getGunHeading());
        ahead(backSize[1]);//Va a la pared de arriba
    }
    
    int retries = 10; //Cantidad de turnos que el robot va a seguir girando el cosito
    public void atacar() {
        //out.println("Im bloodthirsty now :D");
        turnGunLeft(ang);
        ang *= -1;
        retries--;
        if (retries <= 0) {
            modoRobot = Modo.movimiento;
            retries = 10;
            if (getGunHeading() > 270) {
                turnGunLeft(ang);
            }
        }
    }
    
    double distReg;//Guarda la posicion del robot cuando se produjo la interrupcion
    boolean interrumpido = false;//Indica si el robot tuvo que parar para atacar
    public void mover(){
            switch(corner) {
                case 0: //arriba izquierda
                    /*if (interrumpido) {
                        ahead(distReg);
                        interrumpido = false;
                    } else {
                        turnLeft(90);
                        ahead(backSize[0]);
                    }*/
                    cambiarDeEsquina(distReg, 0);
                    break;
                case 2: //abajo derecha
                    /*if (interrumpido) {
                        ahead(backSize[0]-distReg);
                        interrumpido = false;
                    } else {
                        turnLeft(90);
                        ahead(backSize[0]);
                    }*/
                    cambiarDeEsquina(backSize[0]-distReg, 0);
                    break;
                case 1: //abajo izquierda
                    /*if (interrumpido) {
                        ahead(distReg);
                        interrumpido = false;
                    } else {
                        turnLeft(90);
                        ahead(backSize[1]);
                    }*/
                    cambiarDeEsquina(distReg, 1);
                    break;
                default: //arriba derecha
                    /*if (interrumpido) {
                        ahead(backSize[1]-distReg);
                        interrumpido = false;
                    } else {
                        turnLeft(90);
                        ahead(backSize[1]);
                    }*/
                    cambiarDeEsquina(backSize[1]-distReg, 1);
                    break;
            }
            corner = corner < 3? corner+1 : 0;
    }
    
    //eje 0 -> x // eje 1 -> y
    void cambiarDeEsquina(double distInt, int eje) {
        if (interrumpido) {
            ahead(distInt);
            interrumpido = false;
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
        if (modoRobot != Modo.attack) {
            modoRobot = Modo.attack;
            interrumpido = true;
            if (corner == 2 || corner == 0) {
                distReg = getX();
            } else if (corner == 1 || corner == 3) {
                distReg = getY();
            }
            stop();
        } else {
            retries++;
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
