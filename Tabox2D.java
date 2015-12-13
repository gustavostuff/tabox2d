/*
    Copyright (c) 2015 Gustavo Alberto Lara Gómez

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/

package com.tavuntu.example;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Tabox2D, singleton class for body-texture management
 */
public class Tabox2D {

    public static boolean debug = false;

    private static HashMap<String, Float> polyInfo;

    private static Tabox2D instance;
    private Box2DDebugRenderer renderer;
    private OrthographicCamera camera;
    private World world;
    private SpriteBatch spriteBath;
    private List<Tabody> tabodies;
    private int width;
    private int height;
    private float meterSize;
    public ShapeRenderer sr = new ShapeRenderer();
    private String filterMin;
    private String filterMag;








    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //  Main functions:
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////


    private Tabox2D(Vector2 gravity) {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        meterSize = 100;// 1 metter = 100px, default.

        pl("device width: " + width + ", device height: " + height);
        polyInfo = new HashMap<String, Float>();

        // Sides by polygon:
        polyInfo.put("triangle", 3f);
        polyInfo.put("square", 4f);
        polyInfo.put("pentagon", 5f);
        polyInfo.put("hexagon", 6f);
        polyInfo.put("heptagon", 7f);
        polyInfo.put("octagon", 8f);

        // Angle of the sides:
        polyInfo.put("triangle_angle", 120f);
        polyInfo.put("square_angle", 90f);
        polyInfo.put("pentagon_angle", 72f);
        polyInfo.put("hexagon_angle", 60f);
        polyInfo.put("heptagon_angle", 51.428f);
        polyInfo.put("octagon_angle", 45f);

        filterMin = "linear";
        filterMag = "linear";

        renderer = new Box2DDebugRenderer();
        spriteBath = new SpriteBatch();
        adjustCamera();
        tabodies = new ArrayList<Tabody>();
        world = new World(new Vector2(gravity.x, gravity.y), true);

        sr = new ShapeRenderer();
    }

    private void adjustCamera() {
        float cameraW = width / meterSize;
        float cameraH = height / meterSize;
        pl("Camera width: " + cameraW + ", Camera height: " + cameraH);
        camera = new OrthographicCamera(cameraW, cameraH);
        // Set at 0, 0, 0 in space:
        float camX = camera.viewportWidth / 2;
        float camY = camera.viewportHeight / 2;
        camera.position.set(camX, camY, 0f);
        camera.update();
    }

    /**
     * Returns and instance of Tabox2D with its own world
     * @return The new instance
     */
    public static Tabox2D getInstance() {
        return getInstance(new Vector2(0, -9.8f));
    }

    /**
     * Get the list of Tabodies in the Tabox2D instance
     * @return List class of tabodies
     */
    public List<Tabody> getTabodies() {
        return tabodies;
    }

    /**
     * Returns a new instance with the given gravity
     * @param gravity World's gravity in the instance
     * @return a new instance
     */
    public static Tabox2D getInstance(Vector2 gravity) {
        if(instance == null) {
            instance = new Tabox2D(gravity);
        }
        return instance;
    }

    /**
     * Set the meter size in pixels
     * @param meterSize Size in pixels
     */
    public void setMeterSize(float meterSize) {
        if(meterSize > 500) {
            perr("setMeterSize(), Max meterSize allowed: 500, " +
                    "using default = 100");
            return;
        }
        this.meterSize = meterSize;
        adjustCamera();
    }

    /**
     * Set the image filter for textures, linear = soft, nearest = pixelated when zooming
     * @param min The minimize mode
     * @param mag The magnify mode
     */
    public void setFilter(String min, String mag) {
        if(!min.equals("linear") && !min.equals("nearest")) {
            perr("setFilter(), 1st parameter must be 'linear' or 'nearest', using 'linear'");
        } else if(!mag.equals("linear") && !mag.equals("nearest")) {
            perr("setFilter(), 2nd parameter must be 'linear' or 'nearest', using 'linear'");
        } else {
            filterMin = min;
            filterMag = mag;
        }
    }

    /**
     * Set debug mode in the simulation, it shows body shapes, AABB centers and centroids
     */
    public void debug() {
        debug = true;
    }








    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //  Ball creator:
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    /**
     * Creates a new Ball (circle shape)
     * @param type "dynamic" or "static"
     * @param x Center X of the ball
     * @param y Center Y of the ball
     * @param r Radius
     * @return A new Tabody instance
     */
    public Tabody newBall(String type, float x, float y, float r) {
        // Scale proportions:
        x = x / meterSize;
        y = y / meterSize;
        r = r / meterSize;

        CircleShape circleShape;
        BodyDef defBall = new BodyDef();

        setType(defBall, type);

        defBall.position.set(x, y);

        Tabody ball = new Tabody();
        ball.body = world.createBody(defBall);
        circleShape = new CircleShape();
        circleShape.setRadius(r);

        FixtureDef fixtureBall = new FixtureDef();
        fixtureBall.shape = circleShape;
        fixtureBall.density = 1;
        fixtureBall.friction = 1;
        fixtureBall.restitution = 0.5f;//0.2f;

        ////////////////////////////////////////
        ball.w = r * 2 * meterSize;
        ball.h = r * 2 * meterSize;
        ball.fixture = fixtureBall;
        ball.bodyType = "ball";
        ////////////////////////////////////////

        ball.body.createFixture(fixtureBall);
        circleShape.dispose();
        tabodies.add(ball);
        return ball;
    }






    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //  Box creator:
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    /**
     * Creates a new Box body
     * @param type "dynamic" or "static"
     * @param x Left-bottom corner X of the box
     * @param y Left-bottom corner Y of the box
     * @param w Width of the box
     * @param h Height of the box
     * @return A new Tabody instance
     */
    public Tabody newBox(String type, float x, float y, float w, float h) {
        // Scale proportions:
        x = x / meterSize;
        y = y / meterSize;
        w = w / meterSize;
        h = h / meterSize;

        PolygonShape polygonShape;
        BodyDef defBox = new BodyDef();

        setType(defBox, type);

        defBox.position.set(x + w / 2, y + h / 2);

        Tabody box = new Tabody();
        box.body = world.createBody(defBox);
        polygonShape = new PolygonShape();
        polygonShape.setAsBox(w / 2, h / 2);

        FixtureDef fixtureBox = new FixtureDef();
        fixtureBox.shape = polygonShape;
        fixtureBox.density = 1;
        fixtureBox.friction = 1;
        fixtureBox.restitution = 0.2f;

        ////////////////////////////////////////
        box.w = w * meterSize;
        box.h = h * meterSize;
        box.fixture = fixtureBox;
        box.bodyType = "box";
        ////////////////////////////////////////

        box.body.createFixture(fixtureBox);
        polygonShape.dispose();
        tabodies.add(box);
        return box;
    }






    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //  Polygons creators:
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    /**
     * Creates an Equilateral triangle with centroid = center param
     * @param type "dynamic" or "static"
     * @param center Center of the regular polygon
     * @param radius Radius of the polygon
     * @return A new Tabody instance
     */
    public Tabody newTriangle(String type, Vector2 center, float radius) {
        return generateRegularPoly("triangle", type, center, radius);
    }

    /**
     * Creates a Square with centroid = center
     * @param type "dynamic" or "static"
     * @param center Center of the polygon
     * @param radius Radius of the polygon
     * @return A new Tabody instance
     */
    public Tabody newSquare(String type, Vector2 center, float radius) {
        return generateRegularPoly("square", type, center, radius);
    }

    /**
     * Creates a Pentagon with centroid = center
     * @param type "dynamic" or "static"
     * @param center Center of the polygon
     * @param radius Radius of the polygon
     * @return A new Tabody instance
     */
    public Tabody newPentagon(String type, Vector2 center, float radius) {
        return generateRegularPoly("pentagon", type, center, radius);
    }

    /**
     * Creates an Hexagon with centroid = center
     * @param type "dynamic" or "static"
     * @param center Center of the polygon
     * @param radius Radius of the polygon
     * @return A new Tabody instance
     */
    public Tabody newHexagon(String type, Vector2 center, float radius) {
        return generateRegularPoly("hexagon", type, center, radius);
    }

    /**
     * Creates an Heptagon with centroid = center
     * @param type "dynamic" or "static"
     * @param center Center of the polygon
     * @param radius Radius of the polygon
     * @return A new Tabody instance
     */
    public Tabody newHeptagon(String type, Vector2 center, float radius) {
        return generateRegularPoly("heptagon", type, center, radius);
    }

    /**
     * Creates an Octagon with centroid = center
     * @param type "dynamic" or "static"
     * @param center Center of the polygon
     * @param radius Radius of the polygon
     * @return A new Tabody instance
     */
    public Tabody newOctagon(String type, Vector2 center, float radius) {
        return generateRegularPoly("octagon", type, center, radius);
    }

    private Tabody generateRegularPoly(String name, String type, Vector2 center, float rad) {
        // Scale proportions:
        center.set(center.x / meterSize, center.y / meterSize);
        rad /= meterSize;

        PolygonShape polygonShape;
        BodyDef defPoly = new BodyDef();

        setType(defPoly, type);

        // Generate points:
        List<Vector2> pts = new ArrayList<Vector2>();
        Vector2 p0 = new Vector2(0, rad);

        float conv = MathUtils.degreesToRadians;
        float angleInDeg = polyInfo.get(name + "_angle");
        float cos = MathUtils.cos(conv * angleInDeg);
        float sin = MathUtils.sin(conv * angleInDeg);

        for(int i = 0; i < polyInfo.get(name); i++) {
            pts.add(new Vector2(p0.x, p0.y));
            p0.set(p0.x, p0.y);

            float newX = p0.x * cos - p0.y * sin;
            float newY = p0.x * sin + p0.y * cos;

            p0.x = newX;
            p0.y = newY;
        }

        // Get bounding box:

        float[] rawPoints = new float[pts.size() * 2];
        int pointIndex = 0;
        for(int i = 0; i < rawPoints.length - 1; i += 2) {
            rawPoints[i] = pts.get(pointIndex).x;
            rawPoints[i + 1] = pts.get(pointIndex).y;
            pointIndex++;
        }

        Polygon polyForBox = new Polygon();
        polyForBox.setVertices(rawPoints);

        //polyForBox.translate(center.x, center.y);

        Rectangle boundingRect = polyForBox.getBoundingRectangle();
        float boxX = boundingRect.x;
        float boxY = boundingRect.y;
        float boxW = boundingRect.getWidth();
        float boxH = boundingRect.getHeight();

        Vector2 aabbCenter = new Vector2(boxX + boxW / 2, boxY + boxH / 2);
        Vector2 centroid = centroidOf(pts);

        pf("aabb Center x: %f\n", aabbCenter.x);
        pf("aabb Center y: %f\n", aabbCenter.y);

        pf("centroid x: %f\n", centroid.x);
        pf("centroid y: %f\n", centroid.y);


        defPoly.position.set(0, 0);

        defPoly.position.set(center.x, center.y);

        Tabody regularPoly = new Tabody();
        regularPoly.body = world.createBody(defPoly);
        //regularPoly.body.setFixedRotation(true);
        polygonShape = new PolygonShape();

        //polygonShape.setAsBox(w / 2, h / 2);
        for(int i = 0; i < rawPoints.length - 1; i += 2) {
            rawPoints[i] -= aabbCenter.x;
            rawPoints[i + 1] -= aabbCenter.y;
        }
        //rawPoints[0] += 0.5;
        polygonShape.set(rawPoints);

        FixtureDef fixtureBox = new FixtureDef();
        fixtureBox.shape = polygonShape;
        fixtureBox.density = 1;
        fixtureBox.friction = 1;
        fixtureBox.restitution = 0.1f;

        ////////////////////////////////////////
        regularPoly.w = boxW * meterSize;//radius * 2 * meterSize;
        regularPoly.h = boxH * meterSize;//radius * 2 * meterSize;
        regularPoly.fixture = fixtureBox;
        regularPoly.bodyType = "poly";
        ////////////////////////////////////////

        regularPoly.body.createFixture(fixtureBox);
        polygonShape.dispose();
        tabodies.add(regularPoly);
        return regularPoly;
    }




    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //  Polygon creator:
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    /**
     * Creates a Polygons with the given points
     * @param type "dynamic" or "static"
     * @param pts points for the polygon
     * @return A new Tabody instance
     */
    public Tabody newPoly(String type, float[] pts) {
        // Scale proportions:
        for(int i = 0; i < pts.length; i++) {
            pts[i] /= meterSize;
        }

        PolygonShape polygonShape;
        BodyDef defPoly = new BodyDef();

        setType(defPoly, type);

        // Get bounding box:

        Polygon polyForBox = new Polygon();
        polyForBox.setVertices(pts);

        //polyForBox.translate(center.x, center.y);

        Rectangle boundingRect = polyForBox.getBoundingRectangle();
        float boxX = boundingRect.x;
        float boxY = boundingRect.y;
        float boxW = boundingRect.getWidth();
        float boxH = boundingRect.getHeight();

        Vector2 aabbCenter = new Vector2(boxX + boxW / 2, boxY + boxH / 2);
        Vector2 centroid = centroidOf(pts);

        pf("aabb Center x: %f\n", aabbCenter.x);
        pf("aabb Center y: %f\n", aabbCenter.y);

        pf("centroid x: %f\n", centroid.x);
        pf("centroid y: %f\n", centroid.y);


        defPoly.position.set(0, 0);

        defPoly.position.set(aabbCenter.x, aabbCenter.y);

        Tabody regularPoly = new Tabody();
        regularPoly.body = world.createBody(defPoly);
        //regularPoly.body.setFixedRotation(true);
        polygonShape = new PolygonShape();

        //polygonShape.setAsBox(w / 2, h / 2);
        for(int i = 0; i < pts.length - 1; i += 2) {
            pts[i] -= aabbCenter.x;
            pts[i + 1] -= aabbCenter.y;
        }
        //rawPoints[0] += 0.5;
        polygonShape.set(pts);

        FixtureDef fixtureBox = new FixtureDef();
        fixtureBox.shape = polygonShape;
        fixtureBox.density = 1;
        fixtureBox.friction = 1;
        fixtureBox.restitution = 0.1f;

        ////////////////////////////////////////
        regularPoly.w = boxW * meterSize;//radius * 2 * meterSize;
        regularPoly.h = boxH * meterSize;//radius * 2 * meterSize;
        regularPoly.fixture = fixtureBox;
        regularPoly.bodyType = "poly";
        ////////////////////////////////////////

        regularPoly.body.createFixture(fixtureBox);
        polygonShape.dispose();
        tabodies.add(regularPoly);
        return regularPoly;
    }




    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //  Other stuff:
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////

    /**
     * Updates simulation, default delta = Gdx.graphics.getDeltaTime()
     */
    public void update() {
        this.update(Gdx.graphics.getDeltaTime());
    }

    /**
     * Updates the simulation with the given delta time
     * @param delta The delta time to simulate
     */
    public void update(float delta) {
        world.step(delta, 6, 2);
        // Move sprites:
        for(Tabody t : tabodies) {
            if(t.sprite != null) {
                float xb = t.body.getPosition().x * meterSize;
                float yb = t.body.getPosition().y * meterSize;
                xb -= t.sprite.getWidth() / 2;
                yb -= t.sprite.getHeight() / 2;
                t.sprite.setPosition(xb, yb);
                t.sprite.setRotation(t.body.getAngle() * MathUtils.radiansToDegrees);
            }
        }
    }

    /**
     * Dispose the world and Box2DDebugRenderer in the simulation
     */
    public void dispose() {
        world.dispose();
        renderer.dispose();
    }

    /**
     * Draws the world, this is, sprites and Box2DDebugRenderer shapes
     */
    public void draw() {
        // Draw sprites:
        for(Tabody t : tabodies) {
            if(t.sprite != null) {
                spriteBath.begin();
                t.sprite.draw(spriteBath);
                spriteBath.end();
            }
            if(this.debug) {
                renderer.render(world, camera.combined);
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setAutoShapeType(true);
                sr.setColor(Color.RED);
                // Center of mass:
                sr.circle(
                        t.body.getWorldCenter().x * meterSize,
                        t.body.getWorldCenter().y * meterSize, 3);
                // Geometric center:
                sr.setColor(Color.CYAN);
                sr.set(ShapeRenderer.ShapeType.Line);
                sr.circle(
                        t.body.getPosition().x * meterSize,
                        t.body.getPosition().y * meterSize, 3);
                sr.end();
            }
        }
    }

    /**
     * Get centroid of given polygon points
     * @param pts float[] array of points
     * @return a Vector2 instance with the center
     */
    public Vector2 centroidOf(float[] pts) {
        float centroidX = 0, centroidY = 0;
        for(int i = 0; i < pts.length - 1; i += 2) {
            centroidX += pts[i];
            centroidY += pts[i + 1];
        }
        return new Vector2(centroidX / pts.length / 2, centroidY / pts.length / 2);
    }

    /**
     * Get centroid of given polygon points
     * @param pts List<Vector2D> of points
     * @return a Vector2 instance with the center
     */
    public Vector2 centroidOf(List<Vector2> pts) {
        float centroidX = 0, centroidY = 0;
        for(Vector2 vec : pts) {
            centroidX += vec.x;
            centroidY += vec.y;
        }
        return new Vector2(centroidX / pts.size(), centroidY / pts.size());
    }

    private void setType(BodyDef def, String type) {
        type = type.toLowerCase();
        if(type.equals("d")) {
            def.type = BodyDef.BodyType.DynamicBody;
        } else if(type.equals("s")) {
            def.type = BodyDef.BodyType.StaticBody;
        } else if(type.equals("k")) {
            def.type = BodyDef.BodyType.KinematicBody;
        } else {
            perr("'d', 's' or 'k' expected");
        }
    }

    /**
     * Represents a Tabody object<br/>
     * It retains a box2d Body object, the body type and a Sprite
     */
    public class Tabody {
        Body body;
        String bodyType;// "circle", "rectangle" or "polygon".
        Sprite sprite;
        FixtureDef fixture;
        Vector2 geomCenter;
        float w, h;
        float[] vertices;

        /**
         * Impuse in X axis
         * @param impulse The impulse magnitude
         */
        public void impulseX(float impulse) {
            linearImpulse(new Vector2(impulse, 0));
        }

        /**
         * Impuse in Y axis
         * @param impulse The impulse magnitude
         */
        public void impulseY(float impulse) {
            linearImpulse(new Vector2(0, impulse));
        }

        /**
         * Impulse in the given vector
         * @param impulse
         */
        public void linearImpulse(Vector2 impulse) {
            linearImpulse(impulse.x, impulse.y);
        }

        /**
         * Impulse in the given components
         * @param ix
         * @param iy
         */
        public void linearImpulse(float ix, float iy) {
            body.applyLinearImpulse(new Vector2(ix, iy), body.getWorldCenter(), true);
        }

        /**
         * Attach a texture to the Tabody object
         * @param fileNamePath Path or name of the file
         * @param scope Internal "i" or external "e"
         * @param gap Gap (in percentage) of the image width respect to real body dimensions
         * @return Tabody object
         */
        public Tabody texture(String fileNamePath, String scope, float gap) {
            Texture texture = null;
            if(scope.toLowerCase().equals("i")) {
                texture = new Texture(Gdx.files.internal(fileNamePath));
            } else if(scope.toLowerCase().equals("e")) {
                texture = new Texture(Gdx.files.external(fileNamePath));
            } else {
                perr("texture(), second parameter must be 'i' or 'e', using 'i'");
                texture = new Texture(Gdx.files.internal(fileNamePath));
            }

            // Filter for the texture:
            Texture.TextureFilter tMin = null;
            Texture.TextureFilter tMag = null;
            if(filterMin.equals("linear")) {
                tMin = Texture.TextureFilter.Linear;
            } else {
                tMin = Texture.TextureFilter.Nearest;
            }

            if(filterMag.equals("linear")) {
                tMag = Texture.TextureFilter.Linear;
            } else {
                tMag = Texture.TextureFilter.Nearest;
            }
            texture.setFilter(tMin, tMag);

            this.sprite = new Sprite(texture);
            float scaleX = this.w / this.sprite.getWidth() * gap;
            float scaleY = this.h / this.sprite.getHeight() * gap;
            float posX = this.body.getPosition().x * meterSize;
            float posY = this.body.getPosition().y * meterSize;
            sprite.setOrigin(texture.getWidth() / 2, texture.getHeight() / 2);
            sprite.setPosition(posX, posY);
            sprite.setScale(scaleX, scaleY);
            return this;
        }

        /**
         * Attach a texture to the Tabody object
         * @param fileNamePath Path or name of the file
         * @param scope Internal "i" or external "e"
         * @return Tabody object
         */
        public Tabody texture(String fileNamePath, String scope) {
            return texture(fileNamePath, scope, 1.05f);
        }
    }

    

    @Override
    public Object clone() {
        return this;
    }

    private static void pl(Object o){
        System.out.println(o);
    }
    private static void pf(String f, Object o){
        System.out.printf(f, o);
    }

    private static void perr(Object o){
        System.err.println(o);
    }

    private static String nl() {
        return System.getProperty("line.separator");
    }
}
