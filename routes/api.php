<?php

use App\Http\Controllers\LoginController;
use Illuminate\Support\Facades\Route;

Route::post('/login', [LoginController::class, 'login']);
// Add this line to test the API
Route::get('/test', function() {
    return response()->json(['message' => 'API is working']);
});