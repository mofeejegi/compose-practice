/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.librarian.ui.addBook

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.raywenderlich.android.librarian.R
import com.raywenderlich.android.librarian.model.Book
import com.raywenderlich.android.librarian.model.Genre
import com.raywenderlich.android.librarian.model.state.AddBookState
import com.raywenderlich.android.librarian.repository.LibrarianRepository
import com.raywenderlich.android.librarian.ui.composeUi.ActionButton
import com.raywenderlich.android.librarian.ui.composeUi.InputField
import com.raywenderlich.android.librarian.ui.composeUi.TopBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddBookActivity : AppCompatActivity(), AddBookView {

    private val _addBookState = MutableLiveData(AddBookState())
    private val _genreState = MutableLiveData(emptyList<Genre>())

    @Inject
    lateinit var repository: LibrarianRepository

    companion object {
        fun getIntent(context: Context): Intent = Intent(context, AddBookActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AddBookContent() }

        loadGenres()
    }

    private fun loadGenres() {
        lifecycleScope.launch {
            _genreState.value = repository.getGenres()
        }
    }

    @Composable
    private fun AddBookContent() {
        Scaffold(topBar = { AddBookTopBar() }) {
            AddBookFormContent()
        }
    }

    @Composable
    fun AddBookTopBar() {
        TopBar(
            title = stringResource(id = R.string.add_book_title),
            onBackPressed = { onBackPressed() }
        )
    }

    @Composable
    fun AddBookFormContent() {
        val genres = _genreState.value ?: emptyList()
        val isGenresPickerOpen = remember { mutableStateOf(false) }
        val bookNameState = remember { mutableStateOf("") }
        val bookDescriptionState = remember { mutableStateOf("") }
        val selectedGenreName =
            genres.firstOrNull { it.id == _addBookState.value?.genreId }?.name ?: "None"


        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InputField(
                value = bookNameState.value,
                label = stringResource(id = R.string.book_title_hint),
                onStateChanged = {
                    bookNameState.value = it
                    _addBookState.value = _addBookState.value?.copy(name = it)
                })

            InputField(
                value = bookDescriptionState.value,
                label = stringResource(id = R.string.book_description_hint),
                onStateChanged = {
                    bookDescriptionState.value = it
                    _addBookState.value = _addBookState.value?.copy(description = it)
                })

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { isGenresPickerOpen.value = true }) {
                    Text(text = stringResource(id = R.string.genre_select))
                }

                Text(text = selectedGenreName)

                DropdownMenu(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = isGenresPickerOpen.value,
                    onDismissRequest = { isGenresPickerOpen.value = false }) {
                    for (genre in genres) {
                        DropdownMenuItem(onClick = {
                            _addBookState.value = _addBookState.value?.copy(genreId = genre.id)
                            isGenresPickerOpen.value = false
                        }) {
                            Text(text = genre.name)
                        }
                    }
                }
            }

            ActionButton(
                onClick = { onAddBookTapped() },
                isEnabled = true,
                text = stringResource(id = R.string.add_book_button_text)
            )
        }
    }

    @Preview
    @Composable
    fun Preview() {
        AddBookContent()
    }

    fun onAddBookTapped() {
        val bookState = _addBookState.value ?: return

        if (bookState.name.isNotEmpty() &&
            bookState.description.isNotEmpty() &&
            bookState.genreId.isNotEmpty()
        ) {
            lifecycleScope.launch {
                repository.addBook(
                    Book(
                        name = bookState.name,
                        description = bookState.description,
                        genreId = bookState.genreId
                    )
                )

                onBookAdded()
            }
        }
    }

    override fun onBookAdded() {
        setResult(RESULT_OK)
        finish()
    }
}