package com.raywenderlich.android.cocktails.game.factories

import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.android.cocktails.common.network.Cocktail
import com.raywenderlich.android.cocktails.common.repository.CocktailsRepository
import com.raywenderlich.android.cocktails.common.repository.RepositoryCallback
import com.raywenderlich.android.cocktails.game.factory.CocktailsGameFactory
import com.raywenderlich.android.cocktails.game.factory.CocktailsGameFactoryImpl
import com.raywenderlich.android.cocktails.game.model.Game
import com.raywenderlich.android.cocktails.game.model.Question
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CocktailsGameFactoryUnitTests {

    private lateinit var repository: CocktailsRepository
    private lateinit var factory: CocktailsGameFactory

    private val cocktails = listOf(
        Cocktail("1", "Drink1", "image1"),
        Cocktail("2", "Drink2", "image2"),
        Cocktail("3", "Drink3", "image3"),
        Cocktail("4", "Drink4", "image4")
    )

    @Before
    fun setup() {
        repository = mock()
        factory = CocktailsGameFactoryImpl(repository)
    }

    @Test
    fun buildGame_shouldGetCocktailsFromRepo() {
        factory.buildGame(mock())

        verify(repository).getAlcoholic(any())
    }


    @Test
    fun buildGame_shouldCallOnSuccess() {
        val callback = mock<CocktailsGameFactory.Callback>()
        setUpRepositoryWithCocktails(repository)

        factory.buildGame(callback)

        verify(callback).onSuccess(any())
    }

    /*
        In setUpRepositoryWithCocktails, you are using doAnswer to stub the repository.getAlcoholic() method to always return success
        with a list of cocktails. The doAnswer closure returns an InvocationOnMock type, with which you can spy on its arguments.
        You then get the first argument of the method (which is the callback), and call onSuccess() on it.
     */
    private fun setUpRepositoryWithCocktails(
        repository: CocktailsRepository) {
        doAnswer {
            val callback: RepositoryCallback<List<Cocktail>, String> = it.getArgument(0)
            callback.onSuccess(cocktails)
        }.whenever(repository).getAlcoholic(any())
    }

    @Test
    fun buildGame_shouldCallOnError() {
        val callback = mock<CocktailsGameFactory.Callback>()
        setUpRepositoryWithError(repository)

        factory.buildGame(callback)

        verify(callback).onError()
    }

    private fun setUpRepositoryWithError(
        repository: CocktailsRepository) {
        doAnswer {
            val callback: RepositoryCallback<List<Cocktail>, String>
                    = it.getArgument(0)
            callback.onError("Error")
        }.whenever(repository).getAlcoholic(any())
    }

    @Test
    fun buildGame_shouldGetHighScoreFromRepo() {
        setUpRepositoryWithCocktails(repository)

        factory.buildGame(mock())
        verify(repository).getHighScore()
    }

    @Test
    fun buildGame_shouldBuildGameWithHighScore() {
        setUpRepositoryWithCocktails(repository)
        val highScore = 100
        whenever(repository.getHighScore()).thenReturn(highScore)

        factory.buildGame(object : CocktailsGameFactory.Callback {
            override fun onSuccess(game: Game)
                    = Assert.assertEquals(highScore, game.score.highest)

            override fun onError() = Assert.fail()
        })
    }

    @Test
    fun buildGame_shouldBuildGameWithQuestions() {
        setUpRepositoryWithCocktails(repository)

        factory.buildGame(object : CocktailsGameFactory.Callback {
            override fun onSuccess(game: Game) {
                cocktails.forEach {
                    assertQuestion(game.nextQuestion(),
                        it.strDrink,
                        it.strDrinkThumb)
                }
            }

            override fun onError() = Assert.fail()
        })
    }

    private fun assertQuestion(question: Question?,
                               correctOption: String,
                               imageUrl: String?) {
        Assert.assertNotNull(question)
        Assert.assertEquals(imageUrl, question?.imageUrl)
        Assert.assertEquals(correctOption, question?.correctOption)
        Assert.assertNotEquals(correctOption,
            question?.incorrectOption)
    }


}
