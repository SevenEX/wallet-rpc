package com.ztuo.bc.wallet.web3j;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

@Component
@Lazy(false)
public class PassPhraseUtility {

	public static final String LEXICON_FILE = "/google-10000-english-no-swears.txt";
	public static final String RANDOM_GENERATOR_ALGORITHM = "SHA1PRNG";
	public static final int RANDOM_SEED_BYTES = 8;
	
	private static List<String> word;
	private static int words;
	private static SecureRandom rng;
	
	@PostConstruct
	public void inint() {
		try {
			initWordList();
			initRandomizer();
		} 
		catch (Exception e) {
			throw new RuntimeException("Failed to create a pass phrase utility", e);
		}
	}	
	
	private void initWordList() throws Exception  {
		word = FileUtility.getResourceAsStrings(LEXICON_FILE);
		words = word.size();
	}
	
	private void initRandomizer() throws NoSuchAlgorithmException {
		rng = SecureRandom.getInstance(RANDOM_GENERATOR_ALGORITHM);
		rng.setSeed(rng.generateSeed(RANDOM_SEED_BYTES));
	}
	
	public static String  getPassPhrase(int numberOfWords) {
		if(numberOfWords <= 0) {
			throw new IllegalArgumentException("Phass phrase must consist of at least one word");
		}
		
		String [] phrase = new String[numberOfWords];
		for(int i = 0; i < numberOfWords; i++) {
			phrase[i] = getNextWord();
		}
		
		return String.join(" ", phrase);
	}
	
	private  static String getNextWord() {
		int idx = rng.nextInt(words);
		return word.get(idx);
	}
}
