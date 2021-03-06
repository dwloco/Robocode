package pruebarobot;
import robocode.*;
import java.util.Random;
import java.awt.Color;
import robocode.util.Utils;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * PruebaRobot - a robot by Trotsky
 */
//enum Modo {movimiento, attack, defense};
public class NeoTrotskyBot extends Robot
{
    /**
     * run: PruebaRobot's default behavior
     */
    
    
    
    int direccionInicial;
    int direccion = 1;
    
    double robotSize[] = new double[2];//0 -> x - 1 -> y
    double backSize[] = new double[2];
    
    double dist; //distancia que va a recorrer el robot
    
    //int corner = 0; //0 1 2 3
    
    Modo modoRobot = Modo.movimiento;
    
    double ang = 50; //angulo de rotacion del arma en modo ataque
    double angDefensa = -99999999; //angulo en el que se encuentra el enemigo
    
    int p = 1;//potencia del disparo
    
    
    public void run() {
        robotSize[0] = getWidth();
        robotSize[1] = getHeight();
        backSize[0] = getBattleFieldWidth();
        backSize[1] = getBattleFieldHeight();
        dist = Math.max(backSize[0], backSize[1]);
        setColors(Color.BLACK, Color.gray, Color.GREEN);
        movimientoInicial();
        while(true) {
            if (modoRobot == Modo.movimiento) { 
                mover();
            } else if (modoRobot == Modo.attack) {
                atacar();
            } else if (modoRobot == Modo.defense) {
                defender();
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
        }
        turnGunLeft(90);//Apunta para abajo
        ahead(dist);//Va a la pared de arriba
        movInicFinalizado = true;
    }
    
    int retries = 10; //Cantidad de turnos que el robot va a seguir girando el cosito
    public void atacar() {
        turnGunLeft(ang);
        ang *= -1;
        retries--;
        if (retries <= 0) {
            modoRobot = Modo.movimiento;
            retries = 10;
            turnGunRight(-getGunHeading()+getHeading()-90);
        }
    }
    
    double distReg;//Guarda la posicion del robot cuando se produjo la interrupcion
    boolean interrumpido = false;//Indica si el robot tuvo que parar para atacar
    boolean girando = false; //Si el robot se encuentra girando y encuentra un enemigo mas vale ignorarlo
    public void mover(){
        ahead(dist * direccion);   
    }
    
    
    public void defender() {
        p *= 4;
        scan();
        if (!encontrado) {
            modoRobot = Modo.movimiento;
            turnGunRight(-getGunHeading()+getHeading()-90);
        }
    }
    
    /**
     * onScannedRobot: What to do when you see another robot
     */
    boolean encontrado = false;
    public void onScannedRobot(ScannedRobotEvent e) {
        if (e.getEnergy() >= getEnergy() && e.getName().equals("Walls")){
            p = 1;
            encontrado = false;
        } else {
            fire(p);
            encontrado = true;
        }
    }

    public void onBulletHit(BulletHitEvent e) {
        p++;
        if (!girando && !choque) {    
            stop();
            contFails = 0;
            if (modoRobot != Modo.attack && modoRobot != Modo.defense && movInicFinalizado) {
                modoRobot = Modo.attack;
                interrumpido = true;
                stop();
            } else {
                retries++;//Si la bala impacta se le da otra oportunidad
            }
        }
    }
    
    int contFails = 0;
    public void onBulletMissed(BulletMissedEvent e) {
        contFails++;
        if (p > 1) {
            p--;    
        }
        if (contFails >= 3) {
            p = 1;
        }
        if (modoRobot == Modo.defense) {
            modoRobot = Modo.movimiento;
            turnGunRight(-getGunHeading()+getHeading()-90);
        }
        
    }

    
    public void onHitByBullet(HitByBulletEvent e) {
        out.println(angDefensa + " - " + e.getBearing() + " - " + modoRobot);
        modoRobot = Modo.defense;
        if (angDefensa != e.getBearing()) {
            interrumpido = true;
            angDefensa = e.getBearing();
            turnGunLeft(-getHeading()-e.getBearing()+getGunHeading());//Apunta al enemigo
        }    
    }


    public void onHitWall(HitWallEvent e) {
        if (!interrumpido) {
            girando = true;
            turnLeft(90*direccion);
            girando = false;
         }  
         interrumpido = false;
         
         Random r = new Random();
         if (r.nextInt()%10 == 0) { //Hay una probabilidad del 10% de que gire pal otro lado
             direccion *= -1;
         }
    }
    
    //double angIHit;
    boolean choque = false;
    public void onHitRobot(HitRobotEvent e) {
        if (modoRobot != Modo.defense) {
            choque = true;
            //angIHit = getGunHeading();
            turnGunLeft(-getHeading()-e.getBearing()+getGunHeading());//Apunta al robot
            fire(p+10);
            turnGunRight(-getGunHeading()+getHeading()-90);//Normaliza el radar
            interrumpido = true;
            direccion *= -1;
            mover();
            choque = false;
        }
    }
    
    
    
}
