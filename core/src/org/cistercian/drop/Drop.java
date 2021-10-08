package org.cistercian.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import org.w3c.dom.Text;
import org.w3c.dom.css.Rect;

import java.math.MathContext;
import java.util.Iterator;

public class Drop extends ApplicationAdapter {
	// constants
	public static final int WORLD_WIDTH = 800;
	public static final int WORLD_HEIGHT = 480;
	public static final int SPRITE_SIZE = 64;
	public static final int RAINDROP_SPEED = 200;
	public static final int RAINDROP_INTERVAL = 1_000_000_000;
	public static final int BUCKET_SPEED = 200;

	// fields
	private Texture dropImage;
	private Texture drwho;
	private Texture bucketImage;
	private Texture dalekImg;
	private Texture tardisimg;
	private Texture checkerImg;
	private Sound dropSound;
	private Sound losesound;
	private Music rainMusic;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Rectangle bucket;
	private boolean won = false;
	private boolean lost = false;
	private Rectangle checker;
	private Array<Rectangle> raindrops;
	private Array<Rectangle> daleks;
	private Rectangle tardis;
	private Array<Rectangle> checkerlist;
	private long lastDropTime;
	private long lastdalektime;


	@Override
	public void create () {
		// load the images for the droplet and the bucket, 64x64 pixels each
		tardisimg = new Texture(Gdx.files.internal("tardis.png"));
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		drwho = new Texture(Gdx.files.internal("drwho.png"));
		bucketImage = new Texture(Gdx.files.internal("droplet.png"));
		dalekImg = new Texture(Gdx.files.internal("testfac2e.png"));
		checkerImg = new Texture(Gdx.files.internal("checker.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		losesound = Gdx.audio.newSound(Gdx.files.internal("lose.mp3"));
		//rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// start the playback of the background music immediately

		// create the camera and SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 16 *64, 16 *64);
		batch = new SpriteBatch();

		// setup the bucket object
		bucket = new Rectangle();
		bucket.x = MathUtils.random(4, 8) * 64;;
		bucket.y = MathUtils.random(4, 8) * 64;;
		bucket.width = SPRITE_SIZE;
		bucket.height = SPRITE_SIZE;

		// setup the raindrops
		raindrops = new Array<Rectangle>();

		daleks = new Array<Rectangle>();

		checkerlist = new Array<Rectangle>();


		for (int i = 0; i < 16*16 ; i++) {
			Rectangle checker = new Rectangle();
			checker.x = i % 16 * 64;
			checker.y =  (int) ( i / 16f) * 64;
			checkerlist.add(checker);
		}
		while (daleks.size < 14) {
			spawnDalek();
		}
	}

	@Override
	public void render () {
		if (bucket.x == 0 && bucket.y == 15 *64) {
			won = true;
		}
		ScreenUtils.clear(0, 0, 0.2f, 1);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		for (Rectangle checker: checkerlist) {
			batch.draw(checkerImg, checker.x, checker.y);
		}
		batch.draw(tardisimg, 0, 15 * 64);
		if (!won && !lost) {
		batch.draw(drwho, bucket.x, bucket.y);
		}
		for (Rectangle raindrop: raindrops) {
			batch.draw(checkerImg, raindrop.x, raindrop.y);
		}
		for (Rectangle dalek: daleks) {
			batch.draw((dalekImg), dalek.x, dalek.y);
		}

		batch.end();

		// have bucket follow the mouse
		if (Gdx.input.justTouched()) {

			
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			if ( touchPos.x > bucket.x &&  (int) touchPos.x < bucket.x + 64 && touchPos.y < bucket.y && touchPos.y > bucket.y - 64) {
				bucket.setPosition(bucket.x - 0, bucket.y - 64);
				dalekstouched();
			}
			else if ( touchPos.x > bucket.x &&  (int) touchPos.x < bucket.x + 64 && touchPos.y < bucket.y + 128 && touchPos.y > bucket.y + 64) {
				bucket.setPosition(bucket.x - 0, bucket.y + 64);
				dalekstouched();
			}
			else if ( touchPos.y > bucket.y &&  (int) touchPos.y < bucket.y + 64 && touchPos.x < bucket.x && touchPos.x > bucket.x - 64) {
				bucket.setPosition(bucket.x - 64,bucket.y - 0);
				dalekstouched();
			}
			else if ( touchPos.y > bucket.y &&  (int) touchPos.y < bucket.y + 64 && touchPos.x < bucket.x+ 128 && touchPos.x > bucket.x + 64) {
				bucket.setPosition(bucket.x + 64, bucket.y - 0);
				dalekstouched();
			}
			else if (touchPos.y < bucket.y && touchPos.y > bucket.y - 64 && touchPos.x < bucket.x+ 128 && touchPos.x > bucket.x + 64) {
				bucket.setPosition(bucket.x + 64, bucket.y - 64);
				dalekstouched();
			}
			else if (touchPos.y < bucket.y + 128 && touchPos.y > bucket.y + 64 && touchPos.x < bucket.x+ 128 && touchPos.x > bucket.x + 64) {
				bucket.setPosition(bucket.x + 64, bucket.y + 64);
				dalekstouched();
			}
			else if (touchPos.y < bucket.y && touchPos.y > bucket.y - 64 &&  touchPos.x < bucket.x && touchPos.x > bucket.x - 64) {
				bucket.setPosition(bucket.x - 64, bucket.y - 64);
				dalekstouched();
			}
			else if (touchPos.y < bucket.y + 128 && touchPos.y > bucket.y + 64 &&  touchPos.x < bucket.x && touchPos.x > bucket.x - 64) {
				bucket.setPosition(bucket.x - 64, bucket.y + 64);
				dalekstouched();
			}

			//if ( touchPos.x > bucket.x &&  (int) touchPos.x < bucket.x + 32 && touchPos.y < bucket.y + 64 && touchPos.y > bucket.y) {
			//	bucket.setPosition(bucket.x - 0, bucket.y + 64);
			//}
		}



		for (int i = 0; i < raindrops.size; i++) {
			Rectangle raindrop = raindrops.get(i);
			raindrop.y -= RAINDROP_SPEED * Gdx.graphics.getDeltaTime();

			// check to see if caught in bucket
			if (raindrop.overlaps(bucket)) {
				dropSound.play();
				raindrops.removeIndex(i);
			}
		}


		for (int i = 0; i < daleks.size; i++) {
			Rectangle raindrop = daleks.get(i);
			//raindrop.x = MathUtils.lerp(raindrop.x, bucket.x, Gdx.graphics.getDeltaTime());
			//raindrop.y = MathUtils.lerp(raindrop.y, bucket.y, Gdx.graphics.getDeltaTime());
			// check to see if caught in bucket
			for (int k = 0; k < daleks.size; k++) {
			if (daleks.get(k) != raindrop) {
			if (raindrop.x == daleks.get(k).x && raindrop.y == daleks.get(k).y) {
				dropSound.play();
				daleks.removeIndex(i);
			}
			if (bucket.overlaps(raindrop)) {
				if (!lost) {
					losesound.play();
				}
				lost = true;
				
			}
		}
		}
		}
	}
	
	@Override
	public void dispose () {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		batch.dispose();
	}

	private void spawnDalek() {
		Rectangle dalek = new Rectangle();
		dalek.x = MathUtils.random(0, 16) * 64;
		dalek.y = MathUtils.random(0, 16) *64;
		dalek.width = SPRITE_SIZE;
		dalek.height = SPRITE_SIZE;
		daleks.add(dalek);
		

	}

	void dalekstouched () {
		if (!won && !lost) {
		for (int i = 0; i < daleks.size; i++) {
			Rectangle raindrop = daleks.get(i);
			if (raindrop.x < bucket.x) {
				raindrop.x += 64;
			}
			 if (raindrop.x > bucket.x) {
				raindrop.x -= 64;
			}
			 if (raindrop.y < bucket.y) {
				raindrop.y += 64;
			}
			 if (raindrop.y > bucket.y) {
				raindrop.y -= 64;
			}
		}
		}
	}
}
