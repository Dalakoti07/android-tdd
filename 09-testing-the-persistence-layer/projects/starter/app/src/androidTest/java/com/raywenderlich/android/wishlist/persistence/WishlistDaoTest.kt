package com.raywenderlich.android.wishlist.persistence

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.raywenderlich.android.wishlist.Wishlist
import com.raywenderlich.android.wishlist.persistance.WishlistDao
import com.raywenderlich.android.wishlist.persistance.WishlistDatabase
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class WishlistDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var wishlistDatabase: WishlistDatabase
    private lateinit var wishlistDao: WishlistDao

    @Before
    fun initDb() {
        // 1
        wishlistDatabase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            WishlistDatabase::class.java).build()
        // 2
        wishlistDao = wishlistDatabase.wishlistDao()
    }

    @After
    fun closeDb() {
        wishlistDatabase.close()
    }

    @Test
    fun getAllReturnsEmptyList() {
        val testObserver: Observer<List<Wishlist>> = mock()
        wishlistDao.getAll().observeForever(testObserver)
        verify(testObserver).onChanged(emptyList())
    }

    /*
        1. Create a couple wishlists and save them to the database. At this point save() does not exist yet, so there will be an error.
        2. Use your mock testObserver again to call getAll().
        3. Create an ArgumentCaptor to capture the value in onChanged(). Using an ArgumentCaptor from Mockito allows you to make
        more complex assertions on a value than equals().
        4. Test that the result from the database is a non empty list. At this point you care that data was saved and not what was saved,
        so you’re checking the list size only.
     */
    @Test
    fun saveWishlistsSavesData() {
        // 1
        val wishlist1 = WishlistFactory.makeWishlist()
        val wishlist2 = WishlistFactory.makeWishlist()
        wishlistDao.save(wishlist1, wishlist2)

        // 2
        val testObserver: Observer<List<Wishlist>> = mock()
        wishlistDao.getAll().observeForever(testObserver)

        // 3
        val listClass =
            ArrayList::class.java as Class<ArrayList<Wishlist>>
        val argumentCaptor = ArgumentCaptor.forClass(listClass)
        // 4
        verify(testObserver).onChanged(argumentCaptor.capture())
        // 5
        assertTrue(argumentCaptor.value.size > 0)
    }

    @Test
    fun getAllRetrievesData() {
        val wishlist1 = WishlistFactory.makeWishlist()
        val wishlist2 = WishlistFactory.makeWishlist()
        wishlistDao.save(wishlist1, wishlist2)

        val testObserver: Observer<List<Wishlist>> = mock()
        wishlistDao.getAll().observeForever(testObserver)

        val listClass =
            ArrayList::class.java as Class<ArrayList<Wishlist>>
        val argumentCaptor = ArgumentCaptor.forClass(listClass)
        verify(testObserver).onChanged(argumentCaptor.capture())
        val capturedArgument = argumentCaptor.value
        assertTrue(capturedArgument
            .containsAll(listOf(wishlist1, wishlist2)))
    }

    @Test
    fun findByIdRetrievesCorrectData() {
        // 1
        val wishlist1 = WishlistFactory.makeWishlist()
        val wishlist2 = WishlistFactory.makeWishlist()
        wishlistDao.save(wishlist1, wishlist2)
        // 2
        val testObserver: Observer<Wishlist> = mock()
        wishlistDao.findById(wishlist2.id).observeForever(testObserver)
        verify(testObserver).onChanged(wishlist2)
    }

}
