import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.URL;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;

abstract class HW5Object {
	double x, y; // 좌표
	Color color; // 색깔
	
	public HW5Object(double _x, double _y, Color c) {
		x = _x;
		y = _y;
		color = c;
	}
	
	abstract void draw(Graphics g); // 그리기
	void update(double dt) {}; // update
	void collisionResolution(HW5Object o) {}; // 충돌 해결
}

class HW5Wall extends HW5Object {
	int w, h; // wall 가로, 세로 길이
	
	public HW5Wall(double _x, double _y, int _w, int _h, Color c) {
		super(_x, _y, c);
		
		w = _w;
		h = _h;
	}

	@Override
	void draw(Graphics g) {
		g.setColor(color);
		g.fillRect((int) x, (int) y, w, h);
	}
}

class HW5Ball extends HW5Object {
	int r;
	double vx, vy; // 속도
	double px, py; // 이전 위치
	
	public HW5Ball(double _x, double _y, Color c) {
		super(_x, _y, c);
		
		r = 6;
		px = _x;
		py = _y;
		
		double speed = 300;
		double angle = Math.random()*(Math.PI/2) - (Math.PI / 4 * 3); 
		vx = speed * Math.cos(angle);
		vy = speed * Math.sin(angle);
	}

	@Override
	void draw(Graphics g) {
		g.setColor(Color.white);
		g.fillOval((int) (x - r), (int) (y - r), r * 2, r * 2);		
	}
	
	@Override
	void update(double dt) {
		// 이전 값 저장 
		px = x;
		py = y;
		
		// update
		x += vx * dt;
		y += vy * dt;
	}

	@Override
	void collisionResolution(HW5Object o) {
		if(o instanceof HW5Wall){ // 벽과 충돌
			HW5Wall wall = (HW5Wall) o;
			
			if (px > wall.x + wall.w + r) { // 왼쪽 벽 충돌
				x = wall.x + wall.w + r;
				vx = -vx; // 방향 바꾸기
			}
			if(px < wall.x - r) { // 오른쪽 벽 충돌
				x = wall.x - r;
				vx = -vx; // 방향 바꾸기
			}
			if(py > wall.y + wall.h + r) { // 벽 아랫쪽과 충돌
				y = wall.y + wall.h + r;
				vy = -vy; // 방향 바꾸기
			}
		}
		
		if(o instanceof HW5Racket) { // 공이 바와 충돌
			HW5Racket racket = (HW5Racket) o;
			
			if(px < racket.x - r) { // 오른쪽
				x = racket.x - r;
				vx = -vx; // 방향 바꾸기
			}
			if (px > racket.x + racket.w + r) { // 왼쪽
				x = racket.x + racket.w + r;
				vx = -vx; // 방향 바꾸기
			}
			if(py < racket.y - r) { // 윗쪽
				y = racket.y - r;
				vy = -vy; // 방향 바꾸기
				
				// 라켓에 공이 닿은 위치에 따라 x 방향(각도) 바꾸기
				int range = racket.w / 5; // 라켓 5등분 (24씩)
				double dist = x - (racket.x + (racket.w / 2)); // 바 중심과 공 거리
				if (dist < -range * 2) { // -60~-48
					vx = 350 * Math.cos(140); // 140도
				} else if(dist < -range) { // -48~-24 
					vx = 350 * Math.cos(110); // 110도
				} else if(dist > range && dist < range*2) { // 24~48
					vx = 350 * Math.cos(70); // 70도
				} else { // 48~60
					vx = 350 * Math.cos(40); // 40도
				}
				
				if ((dist < -range && vx > 0) || (dist > range && vx < 0)) // 라켓 양쪽 끝
					vx = -vx; // 왼쪽에서 온 공은 왼쪽으로, 오른쪽은 오른쪽으로 공 방향 바꾸기
			}
		}
		
		if(o instanceof HW5Brick) { // 공이 벽돌과 충돌
			HW5Brick brick = (HW5Brick) o;
			
			if (px > brick.x + brick.w + r) { // 왼쪽
				x = brick.x + brick.w + r;
				vx = -vx; // 방향 바꾸기
			}
			if(px < brick.x - r) { // 오른쪽
				x = brick.x - r;
				vx = -vx; // 방향 바꾸기
			}
			if(py < brick.y - r) { // 위쪽
				y = brick.y - r;
				vy = -vy; // 방향 바꾸기
			}
			if(py > brick.y + brick.h + r) { // 아랫쪽
				y = brick.y + brick.h + r;
				vy = -vy; // 방향 바꾸기
			}
		}
	}
	
	boolean isCollide(HW5Object o) {
		// 충돌 체크 (공과 충돌 물체 사이의 거리가 반지름 길이일 때)
		if(o instanceof HW5Wall) { // 벽과 충돌
			HW5Wall wall = (HW5Wall) o; // 다운캐스팅
			if (x > (wall.x - r) && x < (wall.x + wall.w + r) // 왼쪽 끝~오른쪽 끝
				&& y > (wall.y - r) && y < (wall.y + wall.h + r)) { // 상단 끝~하단 끝 내부로 들어옴
				return true;
			}
		}
		if(o instanceof HW5Racket) { // 바와 충돌
			HW5Racket racket = (HW5Racket) o;
			if (x > (racket.x - r) && x < (racket.x + racket.w + r) // 왼쪽 끝~오른쪽 끝
				&& y > (racket.y - r) && y < (racket.y + racket.h + r)) { // 상단 끝~하단 끝 내부로 들어옴
				return true;
			}
		}
		if(o instanceof HW5Brick) { // 블럭과 충돌
			HW5Brick brick = (HW5Brick) o;
			if (x > (brick.x - r) && x < (brick.x + brick.w + r) // 왼쪽 끝~오른쪽 끝
				&& y > (brick.y - r) && y < (brick.y + brick.h + r)) { // 상단 끝~하단 끝 내부로 들어옴
				return true;
			}
		}
		return false;
	}
}

class HW5Racket extends HW5Object {
	int w, h;

	public HW5Racket(double _x, double _y, int _w, int _h, Color c) {
		super(_x, _y, c);
		
		w = _w;
		h = _h;
	}

	@Override
	void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		int offset = 2;
		
		Color c1 = new Color(185, 169, 163);
		Color c2 = new Color(149, 124, 115);
		
		GradientPaint gp = new GradientPaint((int) x, (int) y, c1, (int) x, (int) (y + h), c2);
		g2.setPaint(gp);
		g2.fillRoundRect((int) x, (int) y, (int) w, (int) h, offset*2, offset*2);
		
		g2.setColor(color);
		g2.fillRoundRect((int) (x + offset), (int) (y + offset), (int) (w - offset * 2), (int) (h - offset * 2), offset * 2, offset * 2);
	}

	@Override
	void update(double dt) {
		x += dt;
		if (x < 20) x = 20;
		if (x > 766 - w) x = 766 - w;
	}
}

class HW5Brick extends HW5Object {
	int w, h;
	Color c1, c2; // 그라데이션 색상

	public HW5Brick(double _x, double _y, int _w, int _h, Color c) {
		super(_x, _y, c);

		w = _w;
		h = _h;
		
		if(c == Color.yellow) {
			c1 = new Color(255,255,201);
			c2 = new Color(255, 224, 91);
		} else {
			c1 = new Color(210, 224, 242);
			c2 = new Color(155, 187, 215);
		}
	}

	@Override
	void draw(Graphics g) {
		int offset = 7;
		
		Graphics2D g2 = (Graphics2D) g;
		GradientPaint gp = new GradientPaint((int) x, (int) y, c1, (int) x, (int) (y + h), c2);
		g2.setPaint(gp);
		g2.fillRoundRect((int) x, (int) y, (int) w, (int) h, offset * 2, offset * 2);
		
		g2.setColor(color);
		g2.fillRoundRect((int) (x + offset), (int) (y + offset),
				(int) (w - offset * 2), (int) (h - offset * 2), offset * 2, offset * 2);
	}
	
}

abstract class HW5Panel extends JPanel {
	HW5MainPanel main;
	Dimension d;
	
	public HW5Panel(HW5MainPanel panel) {
		main = panel;
	}
	
	abstract void draw(Graphics g); // 화면 그리기
	abstract void setDimension(Dimension _d); // 화면 크기 설정
}

class HW5TitlePanel extends HW5Panel {
	boolean blink = true; // 글씨 깜빡이기
	private Clip clip;
	private URL url;
	
	public HW5TitlePanel(HW5MainPanel panel) {
		super(panel);
		
		try {
			clip = AudioSystem.getClip();
			url = getClass().getClassLoader().getResource("gamestart.wav");
			AudioInputStream audioStream  = AudioSystem.getAudioInputStream(url);
			clip.open(audioStream);
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	@Override
	void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		Font font;
		int sw; // 문자열 가로 길이
		
		String title1 = "Java Programming";
		String title2 = "Homework #5";
		font = new Font("Arial", Font.PLAIN, 40);
		g2.setFont(font);
		g2.setColor(new Color(210, 224, 242));
		sw = (int)font.getStringBounds(title1, g2.getFontRenderContext()).getWidth();
		g2.drawString(title1, (d.width-sw)/2, 170);
		font = new Font("Arial", Font.PLAIN, 45);
		sw = (int)font.getStringBounds(title2, g2.getFontRenderContext()).getWidth();
		g2.drawString(title2, (d.width-sw)/2, 225);
		
		String name = "BLOCK BREAKER";
		font = new Font("Stencil", Font.BOLD, 90);
		g2.setFont(font);
		sw = (int)font.getStringBounds(name, g2.getFontRenderContext()).getWidth();
		g2.setColor(Color.gray);
		g2.drawString(name, (d.width-sw)/2+2, 407);
		g2.setColor(Color.white);
		g2.drawString(name, (d.width-sw)/2, 405);

		if(blink) {
			String notice = "PRESS SPACEBAR TO PLAY!";
			font = new Font("Arial", Font.PLAIN, 25);
			g2.setFont(font);
			sw = (int)font.getStringBounds(notice, g2.getFontRenderContext()).getWidth();
			g2.setColor(Color.gray);
			g2.drawString(notice, (d.width-sw)/2+1, 602);
			g2.setColor(Color.red);
			g2.drawString(notice, (d.width-sw)/2, 600);
		}
	}

	@Override
	void setDimension(Dimension _d) {
		d = _d;
	}
	
	void startBGM() {
		clip.setFramePosition(0);
		clip.start();
	}
	
	void stopBGM() {
		clip.stop();
	}
}

class HW5GamePanel extends HW5Panel {
	private int stage; // 게임 단계
	private int high; // 최고 점수
	private int score; // 현재 점수
	
	private Clip clipR, clipB, clipS; // 라켓, 벽돌, 스테이지 클립
	private URL urlR, urlB, urlS; // sound url
	
	public HW5GamePanel(HW5MainPanel panel) {
		super(panel);

		stage = 1;
		high = 0;
		score = 0;
		
		try {
			clipR = AudioSystem.getClip(); // 라켓 효과음
			urlR = getClass().getClassLoader().getResource("racket_sound.wav");
			AudioInputStream audioStream  = AudioSystem.getAudioInputStream(urlR);
			clipR.open(audioStream);
			
			clipB = AudioSystem.getClip(); // 벽돌 효과음
			urlB = getClass().getClassLoader().getResource("brick_sound.wav");
			audioStream  = AudioSystem.getAudioInputStream(urlB);
			clipB.open(audioStream);
			
			clipS = AudioSystem.getClip(); // 다음 스테이지 효과음
			urlS = getClass().getClassLoader().getResource("levelup.wav");
			audioStream  = AudioSystem.getAudioInputStream(urlS);
			clipS.open(audioStream);
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	@Override
	void draw(Graphics g) {
		try {
			// object 그리기
			Iterator<HW5Object> ito = main.objs.iterator();
			while(ito.hasNext()) {
				ito.next().draw(g);
			}
			Iterator<HW5Ball> itb = main.balls.iterator();
			while(itb.hasNext()) {
				itb.next().draw(g);
			}
		} catch(ConcurrentModificationException e) { }
		
		// score & high score 출력
		Graphics2D g2 = (Graphics2D) g;
		g2.setFont(new Font("Arial", Font.BOLD, 20));
		g2.setColor(Color.lightGray);
		g2.drawString("HIGH: " + high, 40, 705);
		g2.drawString("SCORE: " + score, 40, 735);
	}
	
	@Override
	void setDimension(Dimension _d) {
		d = _d;
	}

	void resetGame() {
		stage = 1;
		score = 0;
	}
	
	int getStage() {
		return stage;
	}
	
	void nextStage() {
		stage++;
	}
	
	int getScore() {
		return score;
	}
	
	int getHighScore() {
		return high;
	}
	
	void earnScore() {
		score += 10;
		if(high < score) high = score;
	}
	
	void racketSound() {
		clipR.setFramePosition(0);
		clipR.start();
	}
	
	void brickSound() {
		clipB.setFramePosition(0);
		clipB.start();
	}
	
	void stageSound() {
		clipS.setFramePosition(0);
		clipS.start();
	}
}

class HW5GameOverPanel extends HW5Panel {
	boolean blink = true; // 글씨 깜빡이기
	
	private Clip clip;
	private URL url;
	
	public HW5GameOverPanel(HW5MainPanel panel) {
		super(panel);
		
		try {
			url = getClass().getClassLoader().getResource("gameover.wav");
			AudioInputStream audioStream;
			audioStream = AudioSystem.getAudioInputStream(url);
			clip = AudioSystem.getClip();
			clip.open(audioStream);
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	@Override
	void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		Font font;
		int sw; // 문자열 가로 길이
		
		String over = "GAME OVER";
		font = new Font("Stencil", Font.BOLD, 100);
		g2.setFont(font);
		sw = (int)font.getStringBounds(over, g2.getFontRenderContext()).getWidth();
		g2.setColor(Color.gray);
		g2.drawString(over, (d.width-sw)/2+2, 262);
		g2.setColor(Color.white);
		g2.drawString(over, (d.width-sw)/2, 260);
		
		String highStr = "HIGH SCORE: " + main.game.getHighScore();
		String yourStr = "YOUR SCORE: " + main.game.getScore();
		font = new Font("Stencil", Font.BOLD, 45);
		g2.setFont(font);
		sw = (int)font.getStringBounds(highStr, g2.getFontRenderContext()).getWidth();
		g2.setColor(Color.gray);
		g2.drawString(highStr, (d.width-sw)/2+2, 402);
		g2.setColor(new Color(210, 224, 242));
		g2.drawString(highStr, (d.width-sw)/2, 400);
			
		sw = (int)font.getStringBounds(yourStr, g2.getFontRenderContext()).getWidth();
		g2.setColor(Color.gray);
		g2.drawString(yourStr, (d.width-sw)/2+2, 462);
		g2.setColor(new Color(210, 224, 242));
		g2.drawString(yourStr, (d.width-sw)/2, 460);
		
		if(blink) {
			String notice = "PRESS SPACEBAR!";
			font = new Font("Arial", Font.PLAIN, 30);
			g2.setFont(font);
			sw = (int)font.getStringBounds(notice, g2.getFontRenderContext()).getWidth();
			g2.setColor(Color.black);
			g2.drawString(notice, (d.width-sw)/2+2, 602);
			g2.setColor(Color.red);
			g2.drawString(notice, (d.width-sw)/2, 600);
		}
	}
	
	@Override
	void setDimension(Dimension _d) {
		d = _d;
	}
	
	void startBGM() {
		clip.setFramePosition(0);
		clip.start();
	}
	
	void stopBGM() {
		clip.stop();
	}
}

class HW5MainPanel extends JPanel implements KeyListener, Runnable {
	HW5TitlePanel title;
	HW5GamePanel game;
	HW5GameOverPanel over;
	Thread t;
	
	LinkedList<HW5Object> objs;
	LinkedList<HW5Ball> balls, newBalls;
	Dimension d;
	
	int mode = 0; // title(0) -> game(1) -> over(2)
	
	public HW5MainPanel() {
		// Panel
		title = new HW5TitlePanel(this);
		game = new HW5GamePanel(this);
		over = new HW5GameOverPanel(this);
		
		// LinkedList
		objs = new LinkedList<>();
		balls = new LinkedList<>();
		newBalls = new LinkedList<>();
		
		// 키보드 이벤트
		setFocusable(true);
		requestFocus();
		addKeyListener(this);
		
		// 스레드
		t = new Thread(this);
		t.start();
		title.startBGM(); // 게임 시작 효과음
	}
	
	@Override
	public void run() {
		while(true) {
			if(mode == 0) {
				try {
					Thread.sleep(250);
					title.blink = !title.blink;
					repaint();
				} catch (InterruptedException e) { return; }
			}	
			if(mode == 1) {
				try {
					updateBall(); // 공 위치 변경
					
					if(balls.size() == 0) { // 남아있는 공 없음
						over.startBGM(); // 게임 오버 효과음
						mode = 2; // game over
					}
					else playGame(game.getStage()); // 공 있음
	
					repaint();
					Thread.sleep(16);
				} catch (InterruptedException e) { return; }
			}
			if(mode == 2) {
				try {
					Thread.sleep(200);
					over.blink = !over.blink;
					repaint();
				} catch (InterruptedException e) { return; }
			}
		}
	}
	
	void updateBall() {
		Iterator<HW5Ball> it = balls.iterator();
		while(it.hasNext()) {
			HW5Ball b = it.next();
			b.update(0.016); // 위치 변경
			if(b.y > getHeight()-b.r) { // 화면 밖으로 나감
				it.remove();
			}
		}
	}
	
	void playGame(int stage) {
		// collision resolution
		int cnt = 0; // 벽돌 개수 체크
		newBalls.clear(); // 새로 추가될 공
		
		Iterator<HW5Ball> itb = balls.iterator(); // 공 체크
		while(itb.hasNext()) { 
			HW5Ball b = itb.next();
			
			Iterator<HW5Object> ito = objs.iterator(); // object 체크
			while(ito.hasNext()) {
				HW5Object o = ito.next();
				
				if(o instanceof HW5Brick)
					cnt++; // 벽돌 존재
				
				if(b.isCollide(o)) { // 공 충돌 체크
					b.collisionResolution(o);
					
					if(o instanceof HW5Racket)
						game.racketSound(); // 라켓 충돌 효과음
					
					if(o instanceof HW5Brick) { // 벽돌과 충돌
						game.brickSound(); // 벽돌 충돌 효과음
						game.earnScore(); // 점수 획득
						ito.remove(); // 벽돌 삭제
						cnt--; // 벽돌 사라짐
						
						if(o.color == Color.yellow) { // 노랑 벽돌인 경우 공 2개 추가
							double speed = 300;
							double angle = Math.acos(b.vx/300); // 공의 원래 각도 구하기
							double offset = Math.PI / 180 * 25; // 각도 25도 변경
							
							HW5Ball new1 = new HW5Ball(b.x, b.y, Color.white);
							new1.vx = speed * Math.cos(angle + offset); // 각도 바꾸기
							new1.vy = speed * Math.sin(angle + offset);
							newBalls.add(new1);
							
							HW5Ball new2 = new HW5Ball(b.x, b.y, Color.white);
							new2.vx = speed * Math.cos(angle - offset); // new1과 각도 반대로
							new2.vy = speed * Math.sin(angle - offset);;
							newBalls.add(new2);
						}
					}
				}
			}
		}
		
		if (cnt == 0) { // 남아있는 벽돌 없으면
			game.nextStage(); // 다음 단계로
			game.stageSound(); // 다음 스테이지 효과음
			
			// object 새로 만들기
			objs.clear();
			makeRacket();
			makeWalls();
			makeBricks();
			
			// 공 새로 만들기
			balls.clear();
			makeBalls();
		} else { // 벽돌 남아있으면
			// 추가한 공 원래 리스트로 옮기기
			for(HW5Ball b : newBalls)
				balls.add(b);
			newBalls.clear();
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		// 배경 그라데이션 그리기
		Color c1 = new Color(4, 7, 19);
		Color c2 = new Color(40, 67, 124);
		GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
		g2.setPaint(gp);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		// 모드에 맞게 그리기
		if(mode == 0) {
			title.setDimension(getSize());
			title.draw(g);
		}
		if(mode == 1) {
			game.setDimension(getSize());
			game.draw(g);
		}
		if(mode==2) {
			over.setDimension(getSize());
			over.draw(g);
		} 
	}
	
	void makeBricks() { // 벽돌 생성
		int s = game.getStage();
		
		int w = 746 / (s * 3);
		int h = 440 / (s * 3);
		int offset = 20; // 벽 너비
		
		Color colors[] = { new Color(177, 202, 232), new Color(177, 202, 232),
				new Color(177, 202, 232), Color.yellow }; // 노랑 벽돌 확률 낮게
		
		for (int i = 0; i < s * 3; i++) {
			for (int j = 0; j < s * 3; j++) {
				int k = (int)(Math.random() * 4);
				objs.add(new HW5Brick(i * w + offset, j * h + offset, w, h, colors[k]));
			}
		}
	}
	
	void makeWalls() { // 벽 생성 
		int size = 20; // 벽 너비
		int w = 786;
		int h = 800; 
		
		objs.add(new HW5Wall(0, 0, w, size, Color.darkGray));
		objs.add(new HW5Wall(0, 0, size, h, Color.darkGray));
		objs.add(new HW5Wall(w - size, 0, size, h, Color.darkGray));
	}
	
	void makeBalls() { // 공 생성
		int w = 786;
		balls.add(new HW5Ball(w/2, 644, Color.white));
	}
	
	void makeRacket() { // 라켓 생성
		int w = 786;
		objs.add(new HW5Racket(w/2-60, 650, 120, 20, new Color(160, 137, 129)));
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			mode = (mode + 1) % 3; // panel 교체
			
			if(mode == 0) { // title
				over.stopBGM();
				title.startBGM();
			}
			if (mode == 1) { // game
				title.stopBGM(); // 시작화면 사운드 stop
				game.resetGame(); // 게임 초기화

				// object 만들기
				objs.clear();
				makeRacket();
				makeWalls();
				makeBricks();

				// 공 만들기
				balls.clear();
				makeBalls();
			}
		}
		if(e.getKeyCode() == KeyEvent.VK_LEFT && mode == 1) { // 게임 중에만 동작
			objs.get(0).update(-15);
			repaint();
		}
		if(e.getKeyCode() == KeyEvent.VK_RIGHT && mode == 1) { // 게임 중에만 동작
			objs.get(0).update(15);
			repaint();
		}
	}

	public void keyTyped(KeyEvent e) { }
	public void keyReleased(KeyEvent e) { }
}

public class JavaHW5 extends JFrame {
	public JavaHW5() {
		setTitle("Homework 4");
		setSize(800, 800);
		setResizable(false);
		
		add(new HW5MainPanel());
		
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		new JavaHW5();
	}
}
