package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class Drop extends ApplicationAdapter {

	OrthographicCamera camera;

	SpriteBatch batch;

	Texture dropImage;
	Texture bucketImage;

	Sound dropSound;
	Music rainMusic;

	Rectangle bucket;
	Vector3 touchPos;

	Array<Rectangle> raindrops;
	long lastDropTime;

	@Override
	public void create() {

		touchPos = new Vector3();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		batch = new SpriteBatch();

		bucketImage = new Texture("bucket.png");
		dropImage = new Texture("droplet.png");

		dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));

		rainMusic.setLooping(true);// музыка будет повторяться
		rainMusic.play();// запуск музыкального сопровождения

		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		raindrops = new Array<Rectangle>();
		spawnRainDrop();


	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1); // устанавливает цвет экрана
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // очищает экран

		camera.update();// обновляет камеру

		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y); //рисуем ведро
		for (Rectangle raindrop : raindrops){
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();

		//метод заставляет двигаться ведро при прикосновении и центрирует ведро относительно точки прикосновения или щелчка мышью
		if (Gdx.input.isTouched()) {//проверяет есть ли прикосновение или нажатие кнопки мыши
			//преобразование координат прикосновения в систему координат камеры
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			//задание этих координат нашей корзине
			bucket.x = (int) (touchPos.x - 64 / 2);
		}

		// отвечает за движение ведра при нажатии кнопок влево и вправо
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		//deltaTime возвращает время прошедшее между последним и текущим кадром в секундах

		// не даем ведру выйти за пределы экрана

		if (bucket.x < 0) bucket.x = 0;
		if (bucket.x > 860 - 64) bucket.x = 860 - 64;

		//метод проверяет время с момента создания посленей капли  и создания новой капли при необходимости
		if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRainDrop();

		// движение капель с постоянной скоростью и удаление ее из массива при достижении нижней части экрана
		Iterator<Rectangle> iterator =  raindrops.iterator();
		while (iterator.hasNext()){
			Rectangle raindrop = iterator.next();
			raindrop.y -= 200* Gdx.graphics.getDeltaTime();
			if (raindrop.y +64 < 0)iterator.remove();
			if (raindrop.overlaps(bucket)){ // проверяет - пересекаются ли прямоугольники капель и корзины
				dropSound.play();
				iterator.remove();
			}
		}


	}

// метод создания капли и добавления ее в массив
	private void spawnRainDrop(){
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();// получили время создания капли в наносекундах
	}

	@Override
	public void dispose() {
		super.dispose();
		dropSound.dispose();
		dropImage.dispose();
		bucketImage.dispose();
		rainMusic.dispose();
		batch.dispose();
	}
}
