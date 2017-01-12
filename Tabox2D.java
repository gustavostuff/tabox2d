/*
    Copyright (c) 2015-2017 Gustavo Alberto Lara Gómez

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
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Tabox2D, singleton class for body-texture management
 */
public class Tabox2D {

    private boolean debug = false;

    private static HashMap<String, Float> polyInfo;

    private static Tabox2D instance;
    private Box2DDebugRenderer renderer;
    private OrthographicCamera camera;
    private World world;
    private SpriteBatch spriteBath;
    private List<Tabody> tabodies;
    private ShapeRenderer sr;
    private String filterMin;
    private String filterMag;
    private int width;
    private int height;
    private float meterSize;
    private boolean rawForces;




    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //  Main functions:
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////


    private Tabox2D(Vector2 gravity) {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        meterSize = 100;// 1 metter = 100px, default.
        polyInfo = new HashMap<String, Float>();
        rawForces = false;

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
        sr = new ShapeRenderer();
        spriteBath = new SpriteBatch();
        adjustCamera();
        tabodies = new ArrayList<Tabody>();
        world = new World(new Vector2(gravity.x, gravity.y), true);

        sr = new ShapeRenderer();
    }

    private void adjustCamera() {
        float cameraW = width / meterSize;
        float cameraH = height / meterSize;
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
     * If false, impulse is multiplied by the Tabody mass (false as default)
     * @param b If true, the forces are interpreted as usual in Box2D
     */
    public void setRawForces(boolean b) {
        this.rawForces = b;
    }

    /**
     * Set the meter size in pixels
     * @param meterSize Size in pixels
     */
    public void setMeterSize(float meterSize) {
        if(meterSize > 500) {
            System.err.println("setMeterSize(), Max meterSize allowed: 500, " +
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
            System.err.println("setFilter(), 1st parameter must be 'linear' or 'nearest', using 'linear'");
        } else if(!mag.equals("linear") && !mag.equals("nearest")) {
            System.err.println("setFilter(), 2nd parameter must be 'linear' or 'nearest', using 'linear'");
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
        fixtureBall.restitution = 0;

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
        fixtureBox.restitution = 0;

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
     * @param x X Center of the regular polygon
     * @param y Y Center of the regular polygon
     * @param radius Radius of the polygon
     * @return A new Tabody instance
     */
    public Tabody newTriangle(String type, float x, float y, float radius) {
        return generateRegularPoly("triangle", type, x, y, radius);
    }

    /**
     * Creates a Square with centroid = center
     * @param type "dynamic" or "static"
     * @param x X Center of the regular polygon
     * @param y Y Center of the regular polygon
     * @param size Radius of the polygon
     * @return A new Tabody instance
     */
    public Tabody newSquare(String type, float x, float y, float size) {
        return generateRegularPoly("square", type, x, y, size);
    }

    /**
     * Creates a Pentagon with centroid = center
     * @param type "dynamic" or "static"
     * @param x X Center of the regular polygon
     * @param y Y Center of the regular polygon
     * @param radius Radius of the polygon
     * @return A new Tabody instance
     */
    public Tabody newPentagon(String type, float x, float y, float radius) {
        return generateRegularPoly("pentagon", type, x, y, radius);
    }

    /**
     * Creates an Hexagon with centroid = center
     * @param type "dynamic" or "static"
     * @param x X Center of the regular polygon
     * @param y Y Center of the regular polygon
     * @param radius Radius of the polygon
     * @return A new Tabody instance
     */
    public Tabody newHexagon(String type, float x, float y, float radius) {
        return generateRegularPoly("hexagon", type, x, y, radius);
    }

    /**
     * Creates an Heptagon with centroid = center
     * @param type "dynamic" or "static"
     * @param x X Center of the regular polygon
     * @param y Y Center of the regular polygon
     * @param radius Radius of the polygon
     * @return A new Tabody instance
     */
    public Tabody newHeptagon(String type, float x, float y, float radius) {
        return generateRegularPoly("heptagon", type, x, y, radius);
    }

    /**
     * Creates an Octagon with centroid = center
     * @param type "dynamic" or "static"
     * @param x X Center of the regular polygon
     * @param y Y Center of the regular polygon
     * @param radius Radius of the polygon
     * @return A new Tabody instance
     */
    public Tabody newOctagon(String type, float x, float y, float radius) {
        return generateRegularPoly("octagon", type, x, y, radius);
    }

    private Tabody generateRegularPoly(String name, String type, float x, float y, float rad) {
        // Scale proportions:
        x /= meterSize;
        y /= meterSize;
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

        Rectangle boundingRect = polyForBox.getBoundingRectangle();
        float boxX = boundingRect.x;
        float boxY = boundingRect.y;
        float boxW = boundingRect.getWidth();
        float boxH = boundingRect.getHeight();

        Vector2 aabbCenter = new Vector2(boxX + boxW / 2, boxY + boxH / 2);
        defPoly.position.set(x, y);

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
        fixtureBox.restitution = 0;

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

        Rectangle boundingRect = boundingBoxOf(polyForBox.getVertices());
        Vector2 aabbCenter = new Vector2(
                boundingRect.x + boundingRect.width / 2,
                boundingRect.y + boundingRect.height / 2
        );
        defPoly.position.set(aabbCenter.x, aabbCenter.y);

        Tabody regularPoly = new Tabody();
        regularPoly.body = world.createBody(defPoly);
        polygonShape = new PolygonShape();

        for(int i = 0; i < pts.length - 1; i += 2) {
            pts[i] -= aabbCenter.x;
            pts[i + 1] -= aabbCenter.y;
        }
        polygonShape.set(pts);

        FixtureDef fixtureBox = new FixtureDef();
        fixtureBox.shape = polygonShape;
        fixtureBox.density = 1;
        fixtureBox.friction = 1;
        fixtureBox.restitution = 0;

        ////////////////////////////////////////
        regularPoly.w = boundingRect.width * meterSize;//radius * 2 * meterSize;
        regularPoly.h = boundingRect.height * meterSize;//radius * 2 * meterSize;
        regularPoly.fixture = fixtureBox;
        regularPoly.bodyType = "poly";
        ////////////////////////////////////////

        regularPoly.body.createFixture(fixtureBox);
        polygonShape.dispose();
        tabodies.add(regularPoly);
        return regularPoly;
    }

    /**
     * Combines different tabodies in a single one.<br/>
     * This is useful to have a body with different fixtures in an easy way
     * @param tabodyArray Array of tabodies to combine
     * @return A new Tabody
     */
    public Tabody combine(String type, Tabody... tabodyArray) {
        if(tabodyArray.length > 0) {

            Tabody newTabody = new Tabody();

            BodyDef bodyDef = new BodyDef();
            setType(bodyDef, type);

            //List<Vector2> centers = new ArrayList<Vector2>();

            //AABB center of combines bodies:
            List<Vector2> ptsCombined = new ArrayList<Vector2>();
            for(int i = 0; i < tabodyArray.length; i++) {
                //centers.add(tabodyArray[i].body.getWorldCenter());
                Tabody t = tabodyArray[i];
                for(Fixture f : t.body.getFixtureList()) {
                    if(f.getShape() instanceof CircleShape) {
                        CircleShape cS = (CircleShape)f.getShape();
                        //Convert coordinates of circle:
                        Vector2 vec = new Vector2();
                        Body body = f.getBody();

                        Transform transform = body.getTransform();
                        CircleShape shape = (CircleShape) f.getShape();
                        vec.set(shape.getPosition());
                        transform.mul(vec);

                        // top-left and bottom-right of circle bounds:
                        ptsCombined.add(new Vector2(
                                vec.x - cS.getRadius(),
                                vec.y + cS.getRadius()));
                        ptsCombined.add(new Vector2(
                                vec.x + cS.getRadius(),
                                vec.y - cS.getRadius()));
                    } else if(f.getShape() instanceof PolygonShape) {
                        PolygonShape pS = (PolygonShape)f.getShape();
                        // Get points relative to origin of polygon (0, 0):
                        for(int n = 0; n < pS.getVertexCount(); n++){
                            Vector2 tmp = new Vector2();
                            pS.getVertex(n, tmp);
                            ptsCombined.add(new Vector2(
                                    t.body.getPosition().x + tmp.x,
                                    t.body.getPosition().y + tmp.y));
                        }
                    }
                }
            }

            Rectangle rectangle = boundingBoxOf(ptsCombined);

            bodyDef.position.set(
                    rectangle.x + rectangle.width / 2,
                    rectangle.y + rectangle.height / 2);

            newTabody.w = rectangle.width * meterSize;
            newTabody.h = rectangle.height * meterSize;
            newTabody.body = world.createBody(bodyDef);

            for(int i = 0;  i < tabodyArray.length; i++) {

                Tabody t = tabodyArray[i];

                for(Fixture f : t.body.getFixtureList()) {
                    FixtureDef fixtureDef = new FixtureDef();

                    fixtureDef.density = f.getDensity();
                    fixtureDef.friction = f.getFriction();
                    fixtureDef.restitution = f.getRestitution();
                    // Delta X and Y for translating the shapes:
                    float dx = t.body.getWorldCenter().x - bodyDef.position.x;
                    float dy = t.body.getWorldCenter().y - bodyDef.position.y;
                    if(f.getShape() instanceof CircleShape) {

                        CircleShape circleShape = (CircleShape)f.getShape();
                        circleShape.setPosition(new Vector2(
                                circleShape.getPosition().x + dx,
                                circleShape.getPosition().y + dy
                        ));
                        fixtureDef.shape = circleShape;

                    } else if(f.getShape() instanceof PolygonShape) {

                        PolygonShape polygonShape = (PolygonShape)f.getShape();

                        float[] pts = new float[polygonShape.getVertexCount() * 2];
                        int vertexIndex = 0;
                        // delta X and delta Y respect to the main body:
                        dx = t.body.getPosition().x - bodyDef.position.x;
                        dy = t.body.getPosition().y - bodyDef.position.y;
                        for(int j = 0; j < pts.length - 1; j += 2) {
                            Vector2 tmp = new Vector2();
                            polygonShape.getVertex(vertexIndex, tmp);
                            pts[j] = tmp.x + dx;
                            pts[j + 1] = tmp.y + dy;
                            vertexIndex++;
                        }
                        polygonShape.set(pts);
                        fixtureDef.shape = polygonShape;

                    } else if(f.getShape() instanceof EdgeShape) {
                        EdgeShape edgeShape = (EdgeShape)f.getShape();
                        fixtureDef.shape = edgeShape;
                    }

                    newTabody.body.createFixture(fixtureDef);
                }
            }

            // Destroy:
            for(int i = 0; i < tabodyArray.length; i++) {
                world.destroyBody(tabodyArray[i].body);
                tabodies.remove(tabodyArray[i]);
            }

            // Add new Tabody:
            tabodies.add(newTabody);
            return newTabody;
        } else {
            System.err.println("No tabodies specified in Tabox2D.combine()");
            return null;
        }
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
            // Constant force (using velocity vector):
            if(! t.velocity.equals(Vector2.Zero)) {
                t.body.setLinearVelocity(t.velocity);
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
     * Destroys the given Tabody object
     * @param tabody Tabody object to destroy
     */
    public void destroy(Tabody tabody) {
        for(Tabody t : tabodies) {
            if(t.equals(tabody)) {
                world.destroyBody(t.body);
                tabodies.remove(tabody);
                return;
            }
        }
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

    /**
     * Returns the bounding box of the given polygon
     * @param ptsCombined The points as Vector2 list
     * @return A Rectangle object
     */
    private Rectangle boundingBoxOf(List<Vector2> ptsCombined) {
        float[] rawPtsCombined = new float[ptsCombined.size() * 2];
        int ptsCombinedIndex = 0;
        for(int i = 0; i < rawPtsCombined.length - 1; i += 2) {
            rawPtsCombined[i] = ptsCombined.get(ptsCombinedIndex).x;
            rawPtsCombined[i + 1] = ptsCombined.get(ptsCombinedIndex).y;
            ptsCombinedIndex++;
        }

        Polygon polygon = new Polygon();
        polygon.setVertices(rawPtsCombined);
        Rectangle boundingRect = polygon.getBoundingRectangle();
        return boundingRect;
    }

    /**
     * Returns the bounding box of the given polygon
     * @param ptsCombined The points as a float[] object
     * @return A Rectangle object
     */
    private Rectangle boundingBoxOf(float[] ptsCombined) {
        Polygon polygon = new Polygon();
        polygon.setVertices(ptsCombined);
        Rectangle boundingRect = polygon.getBoundingRectangle();
        return boundingRect;
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
            System.err.println("'d', 's' or 'k' expected");
        }
    }

    @Override
    public Object clone() {
        return this;
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
        Vector2 velocity = Vector2.Zero;
        float w, h;

        /**
         * Impuse in X axis
         * @param impulse The impulse magnitude
         * @return This Tabody
         */
        public Tabody impulseX(float impulse) {
            return impulse(new Vector2(impulse, 0));
        }

        /**
         * Impuse in Y axis
         * @param impulse The impulse magnitude
         * @return This Tabody
         */
        public Tabody impulseY(float impulse) {
            return impulse(new Vector2(0, impulse));
        }

        /**
         * Impulse in the given vector
         * @param impulse The impulse magnitude
         * @return This Tabody
         */
        public Tabody impulse(Vector2 impulse) {
            return impulse(impulse.x, impulse.y);
        }

        /**
         * Impulse in the given components
         * @param ix Impulse magnitude in X axis
         * @param iy Impulse magnitude in Y axis
         * @return This Tabody
         */
        public Tabody impulse(float ix, float iy) {
            float forceMultiplier = 1;
            if(!rawForces) {
                forceMultiplier = body.getMass();
            }
            body.applyLinearImpulse(
                    new Vector2(ix * forceMultiplier, iy * forceMultiplier),
                    body.getWorldCenter(), true);
            return this;
        }

        /**
         * Sets a force in X axis
         * @param f The force
         * @return This Tabody
         */
        public Tabody forceX(float f) {
            return force(f, 0);
        }

        /**
         * Sets a force in Y axis
         * @param f The force
         * @return This Tabody
         */
        public Tabody forceY(float f) {
            return force(0, f);
        }

        /**
         * Sets a force in the given vector
         * @param f The force
         * @return This Tabody
         */
        public Tabody force(Vector2 f) {
            return force(f.x, f.y);
        }

        /**
         * May the force be with this Tabody
         * @param fx Force in X
         * @param fy Force in Y
         * @return This Tabody
         */
        public Tabody force(float fx, float fy) {
            float forceMultiplier = 1;
            if(!rawForces) {
                forceMultiplier = body.getMass();
            }
            velocity = new Vector2(fx, fy);
            return this;
        }

        /**
         * Rotates the Tabody around the AABB centre
         * @param degrees Angle in degrees
         * @return This Tabody
         */
        public Tabody rotate(float degrees) {
            body.setTransform(
                    body.getPosition(),
                    MathUtils.degreesToRadians * degrees
            );
            return this;
        }

        /**
         * Attaches a texture to the Tabody object
         * @param fileNamePath Path or name of the file
         * @param scope Internal "i" or external "e"
         * @param gap Gap (in percentage) of the image width respect to real body dimensions<br/>
         *            2 = texture is the double size of body proportions
         * @return Tabody object
         */
        public Tabody texture(String fileNamePath, String scope, float gap) {
            Texture texture;
            if(scope.toLowerCase().equals("i")) {
                texture = new Texture(Gdx.files.internal(fileNamePath));
            } else if(scope.toLowerCase().equals("e")) {
                texture = new Texture(Gdx.files.external(fileNamePath));
            } else {
                System.err.println("setTexture(), second parameter must be 'i' or 'e', using 'i'");
                texture = new Texture(Gdx.files.internal(fileNamePath));
            }

            // Filter for the setTexture:
            Texture.TextureFilter tMin;
            Texture.TextureFilter tMag;
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
            return texture(fileNamePath, scope, 1f);
        }

        /**
         * Attach a texture to the Tabody object
         * @param fileNamePath Path or name of the file
         * @return Tabody object
         */
        public Tabody texture(String fileNamePath) {
            return texture(fileNamePath, "i", 1.02f);
        }

        /**
         * Sets density of Tabody object
         * @param den Restitution (0 to 1)
         * @return The same Tabody object
         */
        public Tabody density(float den) {
            for(Fixture f : this.body.getFixtureList()) {
                f.setDensity(den);
            }
            return this;
        }

        /**
         * Sets friction of Tabody object
         * @param fri Friction (0 to 1)
         * @return The same Tabody object
         */
        public Tabody friction(float fri) {
            for(Fixture f : this.body.getFixtureList()) {
                f.setFriction(fri);
            }
            return this;
        }

        /**
         * Sets restitution of Tabody object
         * @param rest Restitution (0 to 1)
         * @return The same Tabody object
         */
        public Tabody restitution(float rest) {
            for(Fixture f : this.body.getFixtureList()) {
                f.setRestitution(rest);
            }
            return this;
        }

        public float getMass() {
            return this.body.getMass();
        }
    }
}
