package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Passarinho extends ApplicationAdapter {
	// variáveis da textura
	private SpriteBatch batch;
	private Texture []  passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Texture moedaPrata;
	private Texture moedaOuro;
	private Texture moedaAtual;
	private Texture logo;

	// variáveis da colisão
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;
	private Circle circuloMoeda;

	// variáveis do jogo
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 2;
	private float posicaoInicialVerticalPassaro=0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos=0;
	private int pontuacaoMaxima=0;
	private boolean passouCano=false;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 0;
	private float escalaMoeda= 0.25f;
	private float posicaoMoedaY;
	private float posicaoMoedaX;

	//variáveis de texto
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	//variaveis de som
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;
	Sound coinSound;

	//instancia da preferencia
	Preferences preferencias;
	// variavel da camera e declarando o tamanho da tela
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;
	//iniciando as texturas e objetos
	@Override
	public void create () {
		inicializarTexturas();
		inicializaObjetos();
	}
	//roda a lógica do jogo
	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT| GL20.GL_DEPTH_BUFFER_BIT);
		//limpa a tela
		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}
	//método para pegar as referências dos assents
	private void inicializarTexturas()
	{
		//inicilizando os valores das texturas
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
		moedaPrata = new Texture("moedaprata.png");
		moedaOuro = new Texture("moedaouro.png");
		moedaAtual = moedaPrata;
		logo = new Texture("logo.png");
	}
	//método para inicializar os valores das variáveis
	private void inicializaObjetos()
	{
		//batch desenha as sprites
		batch = new SpriteBatch();
		//random é uma classe de randomização
		random = new Random();
		//define a largura e posição do dispositivo, passaro, e cano, além do espaço do cano.
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo/2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;
		//Inicializa o texto da pontuação, definindo a cor e o tamanho
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);
		//Inicializa o texto do reiniciar, definindo a cor e o tamanho
		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);
		//Inicializa o texto de pontuação de melhor pontuação, definindo a cor e tamanho
		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);
		//Inicializa os colisores
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();
		circuloMoeda = new Circle();
		// Incializa os sons
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
		coinSound= Gdx.audio.newSound(Gdx.files.internal("coinsound.mp3"));
		//salva no dispositivo
		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima",0);
		//Inicializa a camera, define o tamanho e largura
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
		//orientação do celular
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
		//define a posição das moedas.
		posicaoMoedaY = alturaDispositivo/2;
		posicaoMoedaX = larguraDispositivo/2;
	}
	//define o comportamento do jogo com base no estado atual.
	private void verificarEstadoJogo(){
		//detecta o toque
		boolean toqueTela = Gdx.input.justTouched();
		//Antes de começar a jogabilidade		
		if (estadoJogo == 0) {
			if (toqueTela) {
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
		}
		//roda a lógica da gameplay
		else if (estadoJogo==1)
		{
			if (toqueTela){
				gravidade = -15;
				somVoando.play();
			}
			
			//movimenta os canos e a moeda da direita para a esquerda
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime()*200;
			posicaoMoedaX-=Gdx.graphics.getDeltaTime()*200;
			
			//reseta a posição do cano quando sai da tela
			if (posicaoCanoHorizontal < -canoTopo.getWidth())
			{
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical  =random.nextInt(400) - 200;
				passouCano = false;
			}
			
			//reseta a posição da moeda quando sai da tela
			if (posicaoMoedaX < -moedaAtual.getWidth() * escalaMoeda / 2)
			{
				resetaMoeda();
			}
			
			//Deixa de aplicar a gravidade ao clicar na tela ou se o jogador estiver em contato com a tela
			if (posicaoInicialVerticalPassaro> 0 || toqueTela)
			{
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
			}
			gravidade++;
		}
		//roda a lógica do estado de game over
		else if (estadoJogo == 2)
		{
			//se tiver mais pontos que a pontuação maxima atual, ele atualiza e salva na memória do dispostivo
			if (pontos > pontuacaoMaxima)
			{
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferencias.flush();
			}
			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime()*500;
			//se houver toque na tela, reseta para o estado inicial do jogo

			if (toqueTela)
			{
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo/2;
				posicaoCanoHorizontal = larguraDispositivo;
				resetaMoeda();
			}
		}
	}
	
	//método para resetar a posição da moeda
	private void resetaMoeda()
	{
		posicaoMoedaX = posicaoCanoHorizontal + larguraDispositivo/2;
		posicaoMoedaY= alturaDispositivo/2;
		//Muda p tipo da moeda que irá aparecer aleatoriamente

		if(random.nextInt(99) <= 29) moedaAtual = moedaOuro;
				else moedaAtual = moedaPrata;
	}
	//posiciona os colisores dos objetos e detecta as colisões
	private void  detectarColisoes()
	{
		circuloPassaro.set(
				50 + posicaoHorizontalPassaro + passaros [0].getWidth()/2/2,
				posicaoInicialVerticalPassaro + passaros[0].getHeight()/2/2,
				passaros[0].getWidth()/2/2
		);
		//gerando colisor
		retanguloCanoBaixo.set(
				posicaoCanoHorizontal,
				alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);
		//gerando colisor

		retanguloCanoCima.set(
				posicaoCanoHorizontal, alturaDispositivo/2 + espacoEntreCanos/ 2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()
		);
		//gerando colisor
		circuloMoeda.set(posicaoMoedaX, posicaoMoedaY, moedaAtual.getWidth()*escalaMoeda/2);
		
		//detecta as colisões 
		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		boolean colidiuMoeda = Intersector.overlaps(circuloPassaro, circuloMoeda);
		
		//define o que acontece quando colide com o cano
		if (colidiuCanoCima|| colidiuCanoBaixo){
			if (estadoJogo == 1){
				somColisao.play();
				estadoJogo=2;
			}
		}
		//Define o que acontece com a moeda após colidir
		if (colidiuMoeda == true)
		{
			if(moedaAtual == moedaOuro) pontos += 10;
			else pontos += 5;

			posicaoMoedaY = alturaDispositivo * 2;
			coinSound.play();
		}
	}
	//método para desenhar as texturas
	private void desenharTexturas()
	{
		batch.setProjectionMatrix(camera.combined);
		//posicionamento da camera
		batch.begin();
		//começa o desenho

		batch.draw(fundo,0,0,larguraDispositivo,alturaDispositivo);
		batch.draw(passaros[ (int) variacao], 50 + posicaoHorizontalPassaro,posicaoInicialVerticalPassaro,passaros[ (int) variacao].getWidth()/2, passaros[ (int) variacao].getHeight()/2);
		batch.draw(canoBaixo, posicaoCanoHorizontal,
				alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal,
				alturaDispositivo/2+espacoEntreCanos/2+posicaoCanoVertical);
		textoPontuacao.draw(batch,String.valueOf(pontos), larguraDispositivo/2, alturaDispositivo-110);
		
		
		batch.draw(moedaAtual,posicaoMoedaX-moedaAtual.getWidth()*escalaMoeda/2, posicaoMoedaY-moedaAtual.getHeight()*escalaMoeda/2, moedaAtual.getWidth()*escalaMoeda,moedaAtual.getHeight()*escalaMoeda);
		//desenha as texturas

		
		if (estadoJogo == 0)
		{
			batch.draw(logo, larguraDispositivo / 2 - logo.getWidth() / 2, alturaDispositivo / 2);
		}
		//desenha a logo enquanto espera o jogo começar

		if (estadoJogo ==2 )
		{
			batch.draw(gameOver, larguraDispositivo/2 - gameOver.getWidth()/2, alturaDispositivo/2);
			textoReiniciar.draw(batch, "Toque para reiniciar", larguraDispositivo/2 - 140, alturaDispositivo/2 - gameOver.getHeight()/2);
			textoMelhorPontuacao.draw(batch, "Seu record é: " + pontuacaoMaxima + " pontos", larguraDispositivo/2 -140, alturaDispositivo/2 - gameOver.getHeight());
		}
		//durante o estado de perda, desenha na tela o gameOver, toque para reiniciar, record e pontos

		batch.end();
	}
	//método para validação de pontos
	public void validarPontos()
	{
		//Quando o cano sai da tela, vai contar a pontuação.
		if (posicaoCanoHorizontal < posicaoHorizontalPassaro){
			if (!passouCano) {

				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}
		variacao+= Gdx.graphics.getDeltaTime() * 10;
		//variável usada para fazer a animação do pássaro

		if (variacao>3)
			variacao = 0;
	}
	//ele vai redimensionar a viewport com base na resolução do dispositivo.
	@Override
	public void resize(int width, int height){
		viewport.update(width, height);
	}
	//roda quando o aplicativo é fechado
	@Override
	public void dispose () {

	}
}
