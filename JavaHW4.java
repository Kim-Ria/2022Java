import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class Hw4LabelPanel extends JPanel {
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g; // 다운캐스팅

		// Panel 영역 크기 구하기
		Dimension d = getSize();
		int p = d.width / 20; // 세 번째 사각형 
		
		// 첫 번째 세로 그라데이션 그리기
		GradientPaint gp = new GradientPaint(0, 0, Color.white, 0, d.height, Color.lightGray);
		g2.setPaint(gp);
		g2.fillRect(0, 0, d.width, d.height); // Hw4Panel 영역 꽉 채우기
		
		// 두 번째 세로 그라데이션 그리기
		Color c1 = new Color(150, 150, 150);
		Color c2 = new Color(100, 100, 100);
		gp = new GradientPaint(0, 0, c1, 0, d.height, c2);
		g2.setPaint(gp);
		g2.fillRect(3, 3, d.width-6, d.height-6); // 상하좌우 3pixel씩 띄워서 가운데
		
		// 세 번째 세로 그라데이션 그리기
		c1 = new Color(100, 110, 100);
		c2 = new Color(150, 160, 150);
		gp = new GradientPaint(0, 0, c1, 0, d.height, c2);
		g2.setPaint(gp);
		g2.fillRect(d.width/100+p, d.width/100+p, d.width-(d.width/50)-(p*2), d.height-(d.width/50)-(p*2)); // 상하좌우 p만큼씩 띄워서 가운데
	}
}

class Hw4Label extends JLabel {
	String label;
	
	Hw4Label(String s) { // 생성자
		label = s;
	}
	
	void setLabel(String s) {
		label = s;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
		// Label 영역 크기 구하기
		Dimension d = getSize();
		int p = d.width/20 + 15; // 라벨이 세번째 배경 사각형 끝나는 지점보다 안쪽에 그려지도록
		
		// font 설정
		Font font = new Font("Arial", Font.PLAIN, d.width/5);
		g2.setFont(font);
		
		// label을 해당 폰트로 표시했을 때 가로, 세로 길이 구하기
		int lw = (int) font.getStringBounds(label, g2.getFontRenderContext()).getWidth();
		int lh = (int) font.getStringBounds(label, g2.getFontRenderContext()).getHeight();
		
		// label 문자열 그림자 그리기
		g2.setColor(Color.white);
		g2.drawString(label, d.width-lw-p, d.height/2+lh/3+1);
		
		// label 문자열 그리기
		g2.setColor(new Color(50, 60, 50));
		g2.drawString(label, d.width-lw-p, d.height/2+lh/3);
		
	}
}

class Hw4ButtonPanel extends JPanel {
	Hw4ButtonPanel() { // 생성자
		setLayout(new GridLayout(4, 4)); // 4*4 그리드 레이아웃
	}
}

class Hw4Button extends JButton {
	private String text;
	private Color color;
	
	Hw4Button(String s, Color c) {
		super(s);
		color = c;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g; // 다운캐스팅
		
		// 버튼 영역 크기
		Dimension d = getSize();
		int p = d.width / 12; // 버튼 그리기 기준점
		
		// 버튼 배경 그리기
		g2.setColor(new Color(230, 220, 200)); // 배경 색상
		g2.fillRoundRect(p, p, d.width - (p*2), d.height - (p*2), p, p);
		
		// 버튼 Font 설정
		g2.setFont(new Font("Arial", Font.BOLD, (int)(d.height/1.6)));
		
		// 버튼 text 그림자 그리기
		g2.setColor(Color.white);
		g2.drawString(getText(), (int) (d.width / 2 - (p * 1.7) - 1), (int) (d.height / 2 + (p * 2.3) + 2));
		
		// 버튼 text 그리기
		g2.setColor(color); // 설정된 색상
		g2.drawString(getText(), (int) (d.width / 2 - (p * 1.7)), (int) (d.height / 2 + (p * 2.3)));
	}
}

class Hw4Panel extends JPanel implements ComponentListener, ActionListener {
	// Panel
	Hw4LabelPanel lp;
	Hw4ButtonPanel bp;
	
	// Component
	Hw4Label label;
	JButton btns[];
	
	// 계산기
	LinkedList<String> cal = new LinkedList<>(); // 연산 순서대로 저장 (숫자 문자열 & 연산자)
	String num = ""; // 버튼 순서대로 문자열로 저장
	int result = 0; // 계산 결과
	
	Hw4Panel() { // 생성자
		// Panel 생성
		lp = new Hw4LabelPanel();
		bp = new Hw4ButtonPanel();
		
		// Component 생성
		label = new Hw4Label("0"); // 계산기 초기값 0
		btns = new JButton[16]; // 버튼 16개
		
		// 라벨 추가
		lp.add(label); // Hw4LabelPanel에 라벨 추가
		
		// 버튼 생성 & 추가
		String btnTexts[] = { "7", "8", "9", "C", "4", "5", "6", "+", "1", "2", "3", "-", "0", " ", " ", "=" }; // 버튼 Text 배열
		for(int i=0; i<16; i++) {
			if(btnTexts[i].equals(" ")) // 기능 X
				btns[i] = new JButton(); // 기본 버튼
			else if (btnTexts[i].equals("C")) { // 리셋 버튼
				btns[i] = new Hw4Button(btnTexts[i], new Color(200, 120, 100)); // 붉은 색상
				btns[i].addActionListener(this);
			} else { // 숫자 & 연산자
				btns[i] = new Hw4Button(btnTexts[i], new Color(130, 120, 100)); // 브라운 색상
				btns[i].addActionListener(this);
			}
			
			bp.add(btns[i]); // Hw4ButtonPanel에 버튼 추가 
		}
		
		// Panel 추가
		add(lp); // Hw4Panel에 Hw4LabelPanel 추가
		add(bp); // Hw4Panel에 Hw4ButtonPanel 추가
		
		// ComponentListener 등록
		addComponentListener(this);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		double fRatio = 1.35; // 가로 370 * 세로 500 비율
		double pRatio = 0.34; // 라벨 패널 1 : 버튼 패널 2 세로 비율
		
		Dimension d = getParent().getSize(); // Frame 크기 알아내기
		
		int w = d.width; // Panel 가로 길이=Frame의 가로 길이
		int h = (int)(w*fRatio); // Panel 세로 길이=가로길이*비율
		
		if(h > d.height) { // 세로가 Frame의 세로 길이를 넘어가는 경우
			h = d.height; // 세로=Frame의 세로 길이
			w = (int)(h/fRatio); // 가로=세로길이/비율
		}
		
		// Hw4Panel 크기 조정
		setBounds(d.width/2-w/2, d.height/2-h/2, w, h); // 시작점(x,y), 크기(w,h)
		
		// 라벨 Panel 크기 조정
		lp.setPreferredSize(new Dimension(w, (int)(h*pRatio))); // 세로 길이 1:2 비율 (1)
		lp.setBounds(0, 0, w, (int)(h*pRatio)); 
	
		// 버튼 Panel 크기 조정
		bp.setPreferredSize(new Dimension(w, (int)(h*(1-pRatio)))); // 세로 길이 1:2 비율 (2)
		bp.setBounds(0, (int)(h*pRatio), w, (int)(h*(1-pRatio))); // 라벨 Panel 아래
		
		// 라벨 크기 조정
		label.setPreferredSize(lp.getPreferredSize());
		label.setBounds(lp.getBounds());
	}
	
	public void componentMoved(ComponentEvent e) { }
	public void componentShown(ComponentEvent e) { }
	public void componentHidden(ComponentEvent e) { }

	@Override
	public void actionPerformed(ActionEvent e) {
		String in = ((JButton) e.getSource()).getText(); // 클릭된 버튼의 문자
		String op = ""; // 연산자
		
		if(in.equals("C")) { // reset
			cal.clear(); // 리스트 초기화
			num = "";
			result = 0;
			label.setLabel(result+""); // label에 결과 표시
		} else if(in.equals("+") || in.equals("-") || in.equals("=")) { // 연산
			// LinkedList 마지막 원소 숫자, num도 숫자인 경우: 새로운 계산식 시작
			if(cal.size() != 0 && num.equals("") == false && cal.getLast().equals("+") == false && cal.getLast().equals("-") == false) {
				cal.clear(); // 리스트 초기화
				result = 0;
			}
			
			if(num.equals("") == false) {
				cal.add(num); // 리스트에 숫자 추가
				num = ""; // 숫자 초기화
			}
			
			if(cal.size() == 0) result = 0;
			else result = Integer.parseInt(cal.get(0)); // 첫 번째 원소 꺼내기
			
			for(int i=1; i<cal.size(); i++) { // 모든 원소 체크
				if(cal.get(i).equals("+") || cal.get(i).equals("-")) { // 연산자
					op = cal.get(i);
				} else { // 숫자
					if(op.equals("+")) result += Integer.parseInt(cal.get(i)); // 더하기
					else result -= Integer.parseInt(cal.get(i)); // 빼기
				}
			}
			
			// 연산 완료
			cal.clear(); // 리스트 초기화
			cal.add(result+""); // 연산 결과만 추가
			label.setLabel(result+""); // label에 결과 표시
			
			if(in.equals("+") || in.equals("-"))
				cal.add(in); // 마지막 입력된 문자가 +-인 경우: 계속 연산 위해 리스트에 추가
		} else { // 숫자
			num += in; // 숫자 문자열에 이어붙이기
			label.setLabel(num);
		}
		
		repaint();
	}
}

public class JavaHW4 extends JFrame {
	JavaHW4(){ // 생성자
		setTitle("Homework2");
		setSize(500, 500);
		
		getContentPane().setBackground(Color.black); // 배경색 검정으로 설정
		add(new Hw4Panel()); // Frame에 Panel 추가
		
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		new JavaHW4();
	}
}