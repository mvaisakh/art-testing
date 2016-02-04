/*
 * Copyright (C) 2015 Linaro Limited. Ported to Java from:
 *  https://github.com/WebKit/webkit/blob/master/PerformanceTests/SunSpider/tests/sunspider-1.0.2/access-nbody.js
 *
 * Description:     N-body simulation of a galaxy composed out of a four planets and the sun.
 * Main Focus:      Floating-point operations.
 *
 */

/* The Great Computer Language Shootout
   http://shootout.alioth.debian.org/
   contributed by Isaac Gouy */

// http://benchmarksgame.alioth.debian.org/license.html  (BSD 3-clause license)
// See NOTICE file for license.

package benchmarks.math;

import java.lang.Math;
import java.lang.System;

public class AccessNBody {
  private static final double ACCESS_NBODY_EXPECTED = 1.4677045000258846;
  private static final double DAYS_PER_YEAR = 365.24;
  private static final double SOLAR_MASS = 4 * Math.PI * Math.PI;

  private class Body {
    public double x;
    public double y;
    public double z;
    public double vx;
    public double vy;
    public double vz;
    public double mass;

    Body(double x, double y, double z, double vx, double vy, double vz, double mass) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.vx = vx;
      this.vy = vy;
      this.vz = vz;
      this.mass = mass;
    }

    void offsetMomentum(double px, double py, double pz) {
      this.vx = -px / SOLAR_MASS;
      this.vy = -py / SOLAR_MASS;
      this.vz = -pz / SOLAR_MASS;
    }

  }

  private class NBodySystem {
    private Body[] bodies;
    private double px;
    private double py;
    private double pz;

    NBodySystem(Body[] bodies) {
      this.bodies = bodies;

      for (int i = 0; i < bodies.length; i++) {
        Body currentBody = this.bodies[i];
        double mass = currentBody.mass;
        px += currentBody.vx * mass;
        py += currentBody.vy * mass;
        pz += currentBody.vz * mass;
      }
      this.bodies[0].offsetMomentum(px, py, pz);
    }

    void advance(double dt) {
      double dx;
      double dy;
      double dz;
      double distance;
      double mag;

      for (int i = 0; i < bodies.length; i++) {
        Body bodyI = this.bodies[i];
        for (int j = i + 1; j < bodies.length; j++) {
          Body bodyJ = this.bodies[j];
          dx = bodyI.x - bodyJ.x;
          dy = bodyI.y - bodyJ.y;
          dz = bodyI.z - bodyJ.z;

          distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
          mag = dt / (distance * distance * distance);
          bodyI.vx -= dx * bodyJ.mass * mag;
          bodyI.vy -= dy * bodyJ.mass * mag;
          bodyI.vz -= dz * bodyJ.mass * mag;

          bodyJ.vx += dx * bodyI.mass * mag;
          bodyJ.vy += dy * bodyI.mass * mag;
          bodyJ.vz += dz * bodyI.mass * mag;
        }
      }

      for (int i = 0; i < bodies.length; i++) {
        Body body = this.bodies[i];
        body.x += dt * body.vx;
        body.y += dt * body.vy;
        body.z += dt * body.vz;
      }
    }

    double energy() {
      double dx;
      double dy;
      double dz;
      double distance;
      double e = 0.0;

      for (int i = 0; i < bodies.length; i++) {
        Body bodyI = this.bodies[i];

        e += 0.5 * bodyI.mass
            * (bodyI.vx * bodyI.vx + bodyI.vy * bodyI.vy + bodyI.vz * bodyI.vz);

        for (int j = i + 1; j < bodies.length; j++) {
          Body bodyJ = this.bodies[j];

          dx = bodyI.x - bodyJ.x;
          dy = bodyI.y - bodyJ.y;
          dz = bodyJ.z - bodyJ.z;
          distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
          e -= (bodyI.mass * bodyJ.mass) / distance;
        }
      }
      return e;
    }
  }

  private Body jupiter() {
    return new Body(
        4.84143144246472090e+00,
        -1.16032004402742839e+00,
        -1.03622044471123109e-01,
        1.66007664274403694e-03 * DAYS_PER_YEAR,
        7.69901118419740425e-03 * DAYS_PER_YEAR,
        -6.90460016972063023e-05 * DAYS_PER_YEAR,
        9.54791938424326609e-04 * SOLAR_MASS);
  }

  private Body saturn() {
    return new Body(
        8.34336671824457987e+00,
        4.12479856412430479e+00,
        -4.03523417114321381e-01,
        -2.76742510726862411e-03 * DAYS_PER_YEAR,
        4.99852801234917238e-03 * DAYS_PER_YEAR,
        2.30417297573763929e-05 * DAYS_PER_YEAR,
        2.85885980666130812e-04 * SOLAR_MASS);
  }

  private Body uranus() {
    return new Body(
        1.28943695621391310e+01,
        -1.51111514016986312e+01,
        -2.23307578892655734e-01,
        2.96460137564761618e-03 * DAYS_PER_YEAR,
        2.37847173959480950e-03 * DAYS_PER_YEAR,
        -2.96589568540237556e-05 * DAYS_PER_YEAR,
        4.36624404335156298e-05 * SOLAR_MASS);
  }

  private Body neptune() {
    return new Body(
         1.53796971148509165e+01,
         -2.59193146099879641e+01,
         1.79258772950371181e-01,
         2.68067772490389322e-03 * DAYS_PER_YEAR,
         1.62824170038242295e-03 * DAYS_PER_YEAR,
         -9.51592254519715870e-05 * DAYS_PER_YEAR,
         5.15138902046611451e-05 * SOLAR_MASS);
  }

  private Body sun() {
    return new Body(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
  }

  private double simulateGalaxy() {
    double output = 0.0;
    Body[] bodies = { sun(), jupiter(), saturn(), uranus(), neptune() };
    NBodySystem galaxy = new NBodySystem(bodies);

    for (int i = 3; i <= 24; i *= 2) {
      output += galaxy.energy();
      for (int j = 0; j < i * 100; j++) {
        galaxy.advance(0.01);
      }
      output += galaxy.energy();
    }
    return output;
  }

  public void timeAccessNBody(int iters) {
    for (int i = 0; i < iters; i++) {
      simulateGalaxy();
    }
  }

  public boolean verify() {
    final double output = simulateGalaxy();
    return output == ACCESS_NBODY_EXPECTED;
  }

  public static void main(String[] argv) {
    AccessNBody obj = new AccessNBody();
    final long before = System.currentTimeMillis();
    obj.timeAccessNBody(172);
    final long after = System.currentTimeMillis();
    obj.verify();
    System.out.println("benchmarks/math/AccessNBody: " + (after - before));
  }
}
