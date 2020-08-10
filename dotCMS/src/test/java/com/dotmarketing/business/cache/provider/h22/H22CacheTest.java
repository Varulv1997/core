package com.dotmarketing.business.cache.provider.h22;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import com.dotmarketing.exception.DotRuntimeException;
import com.dotmarketing.util.Logger;
import com.liferay.util.FileUtil;
import io.vavr.control.Try;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class H22CacheTest {

	final String[] GROUPNAMES = { "testGroup", "testGroup2", "myBigGroup" };
	final String KEYNAME = "testKey";
	final String CONTENT = "test my Content!!!";
	final String LONG_GROUPNAME = "thifsais-as-disadisaidsadiias-dsaidisadisaid";
	final String LONG_KEYNAME = new String(
			"A"
					+ "\u00ea"
					+ "\u00f1"
					+ "\u00fc"
					+ "C"
					+ "ASDSADFDDSFasfddsadasdsadsadasdq3r4efwqrrqwerqewrqewrqreqwreqwrqewrqwerqewrqewrqwerqwerqwerqewr43545324542354235243524354325423qwerewds fds fds gf eqgq ewg qeg qe wg egw	ww	eeR ASdsadsadadsadsadsadaqrewq43223t14@#$#@^%$%&%#$sfwf	erqwfewfqewfgqewdsfqewtr243fq43f4q444fa4ferfrearge");;
	final String CANT_CACHE_KEYNAME = "CantCacheMe";
	final int numberOfPuts = 1000;
	final int numberOfThreads = 40;
	final int numberOfGroups = 100;
	final int maxCharOfObjects = 100;
	static H22Cache cache ;
	
	@BeforeClass
	public static void initCache(){
	       // File dir = Files.createTempDir();
        File dir = new File("/tmp/h2cachetest");
        FileUtil.deltree(dir);
        dir.mkdirs();

        cache = new H22Cache(dir.getAbsolutePath());
        try {
            cache.init();
        }
        catch(Exception e) {
            throw new DotRuntimeException(e);
        }
	}
	
	
	
    @Test
    public void test_basic_cache_operations() throws Exception {


        assertThat("Are we the H2 Cache Loader?", "H22Cache".equals(cache.getKey()));

        for (String group : GROUPNAMES) {
            // put content
            cache.put(group, KEYNAME, CONTENT);


            // get content from cache
            assertThat("Did we cache something", CONTENT.equals(getFromCacheLoop(group, KEYNAME)));
            // flush the group and check that we are null
            cache.remove(group);
            assertThat("we should be null", cache.get(group, KEYNAME) == null);

            // put content
            cache.put(group, KEYNAME, CONTENT);

            // get content from cache
            assertThat("Did we cache something", CONTENT.equals(getFromCacheLoop(group, KEYNAME)));

            // remove the keyname
            cache.remove(group, KEYNAME);

            // get content from cache and check that we are null after remove
            assertThat("we should be null after remove", getFromCacheLoop(group, KEYNAME) == null);

        }
    }
	
	
	@Test
	public void test_special_cases_like_long_names() throws Exception {


		// try to cache a log key
		cache.put(LONG_GROUPNAME, LONG_KEYNAME, CONTENT);
		assertThat("We should cache with a long key", CONTENT.equals(getFromCacheLoop(LONG_GROUPNAME, LONG_KEYNAME)));

		// try to remove the long key
		cache.remove(LONG_GROUPNAME);
		assertThat("we should be null after remove", cache.get(LONG_GROUPNAME, KEYNAME) == null);

		// try to cache something that can't be cached
		cache.put(LONG_GROUPNAME, CANT_CACHE_KEYNAME, new CantCacheMeObject());
		assertThat("we should be null because of the CANT_CACHE_ME ", getFromCacheLoop(LONG_GROUPNAME, CANT_CACHE_KEYNAME) == null);

		// try to cache a log key
		cache.put(LONG_GROUPNAME, LONG_KEYNAME, CONTENT);
		assertThat("We should cache with a long key", CONTENT.equals(getFromCacheLoop(LONG_GROUPNAME, LONG_KEYNAME)));

		
        // get content from cache
        assertThat("Did we cache something", CONTENT.equals(getFromCacheLoop(LONG_GROUPNAME, LONG_KEYNAME)));
        
        
		Fqn fqn = new Fqn(LONG_GROUPNAME, LONG_KEYNAME);
		Set<String> keys = cache.getKeys(LONG_GROUPNAME);
		assertThat("Keys should include long key", keys.contains(fqn.id));

		assertThat("Cache not flushed , we have groups", cache.getGroups().size() > 0);
		// Flush all caches
		cache.removeAll();
		assertThat("Cache flushed, we have no groups", cache.getGroups().size() == 0);

	}

	   
        final String getFromCacheLoop(String groupName, String keyName) throws InterruptedException {

            String fromCache = null;
            for (int i = 0; i < 10; i++) {
                fromCache = (String) cache.get(groupName, keyName);
                if (fromCache != null) {
                    return fromCache;
                }
                Thread.sleep(500l);
            }
            return fromCache;
        }
	
	
    @Test
    public void test_multi_threaded_h22_including_recovery() throws Exception{

		ExecutorService pool = Executors.newFixedThreadPool(numberOfThreads);
		boolean dumpCacheInMiddle=false;
		boolean dieOnError=false;

		final List<Throwable> errors = new ArrayList<>();
		for (int i = 0; i < numberOfPuts; i++) {

			//test dumping the db mid test
			if(i==numberOfPuts/2 && dumpCacheInMiddle){
				cache.dispose(true);
			}
			pool.execute(new TestRunner(cache, i, errors));
			

		}
		pool.shutdown();

		while(!pool.isTerminated()){
			if(errors.size()>0 && dieOnError){
				throw new AssertionError(errors.get(0)) ;
			}
			Thread.sleep(500);
		}
		

		int size = cache.getGroups().size();
		for(int i=0;i<20;i++){
			if(size<numberOfGroups){
				Thread.sleep(1000);
				size = cache.getGroups().size();
			}else{
				break;
			}
		}

		Logger.info(this, "Number of groups: " + cache.getGroups().size());
		assertEquals("Cache filled , we should have 100 groups", numberOfGroups,
				cache.getGroups().size());
		cache.remove("group_1");
		size = cache.getGroups().size();
		for(int i=0;i<20;i++){
			if(size==numberOfGroups){
				Thread.sleep(1000);
				size = cache.getGroups().size();
			}else{
				break;
			}
		}
		assertThat("Cache with 1 group removed , we should have 99 groups", cache.getGroups().size() == (numberOfGroups-1));
		//assertThat("Cache filled , we should have 10 in group 1", cache.getKeys("group_1").size() == numberOfPuts / numberOfGroups);

	}
	
	void breakCache() throws SQLException{
		Optional<Connection> conn = cache.createConnection(true, 0);
		if(conn.isPresent()){
			Statement stmt = conn.get().createStatement();
			stmt.execute("drop table " + H22Cache.TABLE_PREFIX + "0");
			conn.get().close();
		}
	}
	
	
	class TestRunner implements Runnable {

		final H22Cache cache;
		final int iter;
		final List<Throwable> errors;
		public TestRunner(H22Cache cache, int iter, List<Throwable> errors){
			this.cache=cache;
			this.iter = iter;
			this.errors = errors;
		}
		
		@Override
		public void run() {

			String group = "group_" + iter % numberOfGroups;
			String key = RandomStringUtils.randomAlphanumeric(20);
			int len = (int) (Math.random() * maxCharOfObjects);
			String val = RandomStringUtils.randomAlphanumeric(len+1);
			cache.put(group, key, val);
			String newVal = null;

			newVal = (String) Try.of(()->getFromCacheLoop(group, key)).getOrNull();
			int testTimes=10;
			// if the put failed (because the cache was rebuilding
			// and initiing, it is possible to get a null value, so we try again
			//
			while(newVal==null && --testTimes > 0){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {

				}
				
				cache.put(group, key, val);
				newVal = (String) Try.of(()->getFromCacheLoop(group, key)).getOrNull();

			}
			try{
				assertThat("Test Cache hit" + group + "-" + key,  val.equals(newVal));
			}
			catch(Throwable t){
				if(errors.size()>100){
					errors.add(null);
				}
				else{
					errors.add(t);
				}
			}


			
		}
	};
	
	
	public final class CantCacheMeObject {
		final String notSerializable = "fail!";

	}

}
