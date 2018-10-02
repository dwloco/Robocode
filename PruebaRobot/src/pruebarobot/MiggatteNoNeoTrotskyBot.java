package pruebarobot;
import robocode.*;
import java.util.Random;
import java.awt.Color;
import robocode.util.Utils;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * PruebaRobot - a robot by Trotsky
 */
enum Modo {movimiento, attack, defense};
public class MiggatteNoNeoTrotskyBot extends Robot
{
    /**
     * run: PruebaRobot's default behavior
     */
    
    int direccionInicial;
    int direccion = 1;
    
    double dist; //distancia que va a recorrer el robot
    
    //int corner = 0; //0 1 2 3
    
    Modo modoRobot = Modo.movimiento;
    
    double ang = 50; //angulo de rotacion del arma en modo ataque
    double angDefensa = -99999999; //angulo en el que se encuentra el enemigo
    
    int p = 1;//potencia del disparo
    
    boolean randomMovementEnabled = false;
    
    public void run() {
        dist = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
        setColors(Color.DARK_GRAY, Color.gray, Color.GREEN);
        movimientoInicial();
        while(true) {
            switch (modoRobot) {
                case movimiento:
                    mover();
                    //doNothing();
                    break;
                case attack:
                    atacar();
                    //doNothing();
                    break;
                case defense:
                    defender();
                    break;
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
        p = 10;
        if (!Escanear()) {
            modoRobot = Modo.movimiento;
            turnGunRight(-getGunHeading()+getHeading()-90);
        }
    }
    
    /**
     * onScannedRobot: What to do when you see another robot
     */
    boolean encontrado = false;//Determina si el robot debe ser atacado
    public void onScannedRobot(ScannedRobotEvent e) {
        if (modoRobot == Modo.defense){ //determina la inimputabilidad del robot
            if (e.getEnergy() > getEnergy()*1.5 && !e.getName().contains("Walls")){ //no conviene pelear contra alguien más grande
                //Ah, y si es walls esto no aplica
                p = 1;
                encontrado = false;
            } else {
                encontrado = true;
            }
        }
        fire(p);
        
        if (e.getName().contains("Walls") && getOthers() == 1) {
            randomMovementEnabled = true;
        }
    }

    public void onBulletHit(BulletHitEvent e) {
        p++;
        if (!girando && !choque) {    
            stop();
            contFails = 0;
            if (modoRobot != Modo.attack && modoRobot != Modo.defense && movInicFinalizado && getOthers() == 1) {
                modoRobot = Modo.attack;
                interrumpido = true;
                
                //stop();
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
        //out.println(angDefensa + " - " + e.getBearing() + " - " + modoRobot);
        modoRobot = Modo.defense;
        //if (angDefensa != e.getBearing()) {
        if (!Escanear()) {
            interrumpido = true;
            angDefensa = e.getBearing();
            ApuntarAlEnemigo(e.getBearing());
            
        }    
    }


    public void onHitWall(HitWallEvent e) {
        if (!interrumpido) {
            girando = true;
            turnLeft(90*direccion);
            girando = false;
         }  
         interrumpido = false;
         
         if (randomMovementEnabled) {
            Random r = new Random();
            if (r.nextInt()%4 == 0) { //Hay una probabilidad del 25% de que gire pal otro lado
                direccion *= -1;
            }
         }
    }

    boolean choque = false;
    public void onHitRobot(HitRobotEvent e) {
        if (modoRobot != Modo.defense) {
            choque = true;
            ApuntarAlEnemigo(e.getBearing());
            if (Escanear()) {
                fire(p+10);
            } else {
                turnGunRight(-getGunHeading()+getHeading()-90);//Normaliza el radar
                interrumpido = true;
                direccion *= -1;
                mover();
                choque = false;
            }
        }
    }
    
    //Se fija si el enemigo esta en el radar
    boolean Escanear() {
        encontrado = false;
        scan();
        return encontrado;
    }
    
    void ApuntarAlEnemigo(double angEnemigo) {
        out.println(angEnemigo + " " + getGunHeading());
        if (angEnemigo < 0){
            turnGunLeft(-getHeading()-angEnemigo+getGunHeading());//Apunta al enemigo
        } else {
            turnGunRight(getHeading()+angEnemigo-getGunHeading());//Apunta al enemigo
        }
    }
    
}
