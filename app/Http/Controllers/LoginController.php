<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Log;

class LoginController extends Controller
{
    public function login(Request $request)
    {
        try {
            Log::info('Login attempt', ['email' => $request->email]); // Add logging

            $credentials = $request->validate([
                'email' => 'required|email',
                'password' => 'required'
            ]);

            if (Auth::attempt($credentials)) {
                $user = Auth::user();
                Log::info('Login successful', ['user' => $user->email]);
                
                return response()->json([
                    'status' => 'success',
                    'message' => 'Login successful',
                    'user' => [
                        'id' => $user->id,
                        'name' => $user->name,
                        'email' => $user->email
                    ]
                ], 200);
            }

            Log::info('Login failed: Invalid credentials');
            return response()->json([
                'status' => 'error',
                'message' => 'Invalid credentials'
            ], 401);

        } catch (\Exception $e) {
            Log::error('Login error: ' . $e->getMessage());
            // Return more detailed error in development
            return response()->json([
                'status' => 'error',
                'message' => 'Server error: ' . $e->getMessage(),
                'trace' => $e->getTraceAsString() // Remove this in production
            ], 500);
        }
    }
}